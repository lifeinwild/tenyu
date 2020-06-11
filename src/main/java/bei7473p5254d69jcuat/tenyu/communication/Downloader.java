package bei7473p5254d69jcuat.tenyu.communication;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.communication.P2P.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.communication.request.HasFile.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.middle.catchup.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.agenda.content.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import bei7473p5254d69jcuat.tenyutalk.file.*;
import glb.*;
import glb.Glb.*;
import glb.util.*;
import glb.util.Bits;
import glb.util.Bits.*;
import jetbrains.exodus.env.*;

/**
 * 近傍からファイルを取得する。
 * ファイル取得に伴い<filename>.writeBitsがファイルと同じフォルダに作成され
 * 分割DLの状況が書き込まれる。
 * レジューム対応かつ分割DL対応。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Downloader implements GlbMemberDynamicState {
	/**
	 * 同じファイルを複数スレッドが同時に扱わないようにする。
	 */
	private static final Set<Path> lock = new HashSet<Path>();

	/**
	 * 定期処理
	 * バックグラウンド処理というイメージで、
	 * 徐々にネットワークにファイルを広げていくための処理。
	 */
	private static ScheduledFuture<?> periodic = null;

	/**
	 * @param f	DLするファイル
	 * @param necessary	動作上必須のファイルか、
	 * P2Pネットワークの安定性や効率性等のためにできればDLしておきたいファイルか
	 * @return	DL可能か。ストレージの空き容量チェック
	 */
	public boolean canDownload(TenyutalkFileMetadataI f, boolean necessary) {
		long space = f.getRelativePath().toFile().getUsableSpace();
		//ファイルをDLできるだけの空き容量があるか
		boolean capacity = false;
		if (space > f.getFileSize()) {
			capacity = true;
		}
		if (!capacity)
			return false;

		//必要でないファイルをDLするだけの豊富な空き容量があるか
		boolean enoughMargin = false;
		long remaining = space - f.getFileSize();
		long threshold = 1024L * 1000L * 1000L * 3L;//3GB
		if (remaining > threshold) {
			enoughMargin = true;
		}
		if (!necessary && !enoughMargin) {
			return false;
		}
		return true;
	}

	/**
	 * 近傍から分散DLする
	 * @param f	DLするファイル
	 * @param necessary		DL必須か
	 * @return	DLに成功したか
	 */
	public void downloadAsync(TenyutalkFileMetadataI f, boolean necessary) {
		Glb.getExecutorSlow().execute(() -> downloadSync(f, necessary));
	}

	/**
	 * 指定されたファイル一覧について順次DLしていく
	 * @param files
	 * @param necessary
	 */
	public void downloadAsync(List<TenyutalkFileMetadataI> files,
			boolean necessary) {
		Glb.getExecutorSlow().execute(() -> downloadSync(files, necessary));
	}

	/**
	 * 指定されたファイル一覧それぞれに非同期DLを開始するので、
	 * ほぼ同時に全ファイルのDLが開始する。
	 * とはいえNetty側の制限がある。
	 *
	 * @param files
	 * @param necessary
	 */
	public void downloadAsyncBurst(List<TenyutalkFileMetadataI> files,
			boolean necessary) {
		for (TenyutalkFileMetadataI f : files) {
			downloadAsync(f, necessary);
		}
	}

	/**
	 * @param files	DL対象ファイル一覧
	 * @param necessary
	 * @return	全て成功したらtrue
	 */
	public boolean downloadSync(List<TenyutalkFileMetadataI> files,
			boolean necessary) {
		boolean r = true;
		for (TenyutalkFileMetadataI f : files) {
			if (!downloadSync(f, necessary)) {
				r = false;
			}
		}
		return r;
	}

	/**
	 * @param f	DL対象ファイル
	 * @param necessary	必須ファイルか
	 * @return	DLに成功したか既にDL済みだったらtrue
	 */
	public boolean downloadSync(TenyutalkFileMetadataI f, boolean necessary) {
		try {
			//もし必須ファイルでないなら
			if (!necessary) {
				//分担範囲でなければDLしない
				if (!Glb.getSubje().getMe().getRange()
						.support(f.getFileHash())) {
					return false;
				}
			}

			//全部分を取得できていたら成功
			if (f.validateFile()) {
				Glb.debug("already downloaded file=" + f);
				return true;
			}

			Glb.getLogger().info("starting download file=" + f);

			synchronized (this) {
				if (lock.contains(f.getRelativePath())) {
					return false;
				}
				lock.add(f.getRelativePath());
			}

			for (int i = 0; i < 3; i++) {
				if (downloadSyncInternal(f, necessary)) {
					Glb.getLogger().info("Succeeded in download file=" + f);
					return true;
				} else {
					Glb.getLogger().warn("Failed to download path="
							+ f.getRelativePathStr());
				}
			}
		} catch (Exception e) {
			Glb.getLogger().error(
					"Failed to download path=" + f.getRelativePathStr(), e);
		} finally {
			lock.remove(f.getRelativePath());
		}
		return false;
	}

	/**
	 * DL処理はある種の最適化問題という事になる。
	 * 最適化問題のソルバーを使うことも検討したがパターン化された方法で
	 * 十分に解決できると判断した。
	 *
	 * @param f
	 * @return	ファイルのDLに成功したか
	 */
	private boolean downloadSyncInternal(TenyutalkFileMetadataI f,
			boolean necessary) {
		//このあたりでビットと言っているのはたいていwriteBitのことで、1ビットあたり
		//WriteBits.unitバイトのデータに相当する

		DownloadCtx ctx = new DownloadCtx();
		ctx.setFile(f);

		//現在の分散DL状況
		ctx.setWriteBits(f.getWriteBitsAndInit());

		if (!canDownload(f, necessary)) {
			return false;
		}

		//未DLのビット数
		if (ctx.getWriteBits() == null) {
			Glb.debug("writeBits is null");
		}
		if (ctx.getWriteBits().getBits() == null) {
			Glb.debug("Bits is null");
		}
		ctx.setNotDownloadedBitCount(
				ctx.getWriteBits().getBits().getZeroBitCount());
		if (ctx.getNotDownloadedBitCount() <= 0) {
			Glb.getLogger().warn("not downloaded bit count is zero",
					new Exception());
			return false;
		}

		//分担範囲からこのファイルを持っている可能性がある近傍を特定する
		List<P2PEdge> neighbors = Glb.getSubje().getNeighborList()
				.getNeighborsByHashConnectedIn5Minute(f.getFileHash(), true);

		if (neighbors == null || neighbors.size() == 0)
			return false;

		//レイテンシソート
		Collections.sort(neighbors, Comparator.comparing(P2PEdge::getLatency));

		//基本問い合わせ先
		ctx.setInitNeighbors(new ReadonlyNeighborList(neighbors));

		//このファイルのどの部分を持っているか近傍に問い合わせる
		AsyncRequestStatesNoRetry<
				HasFile> hasStates = new AsyncRequestStatesNoRetry<>(
						() -> ctx.getInitNeighbors());
		hasStates.requestToAll(to -> {
			HasFile req = new HasFile();
			req.setFile(f);
			return req;
		});
		hasStates.waitAllDone();

		List<AsyncRequestState<HasFile>> onlySuccess = hasStates
				.getOnlySuccess(state -> {
					try {
						if (!(state.getReq().getRes()
								.getContent() instanceof HasFileResponse)) {
							return false;
						}

						HasFileResponse res = (HasFileResponse) state.getReq()
								.getRes().getContent();
						if (res.getWriteBits() == null) {
							return false;
						}

						WriteBits wb = WriteBits
								.deserializeStatic(res.getWriteBits());
						if (wb.getBits().isFilledZero()) {
							return false;
						}
						if (!Arrays.equals(f.getFileHash(), wb.getHash())) {
							return false;
						}

						if (wb.getBits().getLastBitIndex() != ctx.getWriteBits()
								.getBits().getLastBitIndex()) {
							Glb.getLogger().warn("invalid writeBits=" + wb
									+ " edge=" + state.getTo());
							return false;
						}

						return true;
					} catch (Exception e) {
						Glb.getLogger().warn("", e);
						return false;
					}
				});

		Collections.sort(onlySuccess,
				new Comparator<AsyncRequestState<HasFile>>() {
					@Override
					public int compare(AsyncRequestState<HasFile> o1,
							AsyncRequestState<HasFile> o2) {
						P2PEdge e1 = o1.getTo();
						if (e1 == null)
							return -1;
						P2PEdge e2 = o2.getTo();
						if (e2 == null)
							return 1;
						return (int) (e1.getLatency() - e2.getLatency());
					}
				});

		int successCount = onlySuccess.size();
		if (successCount == 0) {
			Glb.getLogger().warn("no node has the file");
			return false;
		}

		//このアルゴリズムのための特殊な値
		int algorithmVal = 0;
		for (int i = 1; i <= successCount; i++) {
			algorithmVal += i;
		}

		//担当が決まったビット数
		int dealedBitCountTotal = 0;
		//近傍毎の担当ビット数
		List<Integer> bitCountByEdge = new ArrayList<>();
		for (int i = successCount; i > 0; i--) {
			double rate = ((double) i) / algorithmVal;
			int c = (int) (Math.floor(rate * ctx.getNotDownloadedBitCount()));
			//最低1は与える
			if (c == 0)
				c = 1;
			bitCountByEdge.add(c);
			dealedBitCountTotal += c;
		}

		//割合による分配の結果、余りが生じる可能性があるので1番目のノードに足す
		int dif = ctx.getNotDownloadedBitCount() - dealedBitCountTotal;
		if (dif > 0) {//floorで計算しているので正の可能性は無い
			int one = bitCountByEdge.get(0);
			bitCountByEdge.set(0, one + dif);
		}

		//低レイテンシノードがあるか
		int lowLatencyCount = 0;
		for (AsyncRequestState<HasFile> req : onlySuccess) {
			if (req.getTo().isLowLatency()) {
				lowLatencyCount++;
			}
		}

		if (lowLatencyCount > 0) {
			//高レイテンシノードは担当していいビット数に上限を作る
			int excess = 0;
			int i = 0;
			for (AsyncRequestState<HasFile> req : onlySuccess) {
				int bitCountMax = bitCountByEdge.get(i);
				if (bitCountMax == 0)
					continue;
				int bitCountMaxByLatency = 1;
				if (!req.getTo().isLowLatency()
						&& bitCountMax > bitCountMaxByLatency) {
					excess += bitCountMax - bitCountMaxByLatency;
					bitCountMax = bitCountMaxByLatency;
					bitCountByEdge.set(i, bitCountMax);
				}
				i++;
			}
			if (excess > 0) {
				i = 0;
				//超過分を低レイテンシなノードに足す
				int add = excess / lowLatencyCount;
				boolean first = true;
				for (AsyncRequestState<HasFile> req : onlySuccess) {
					if (req.getTo().isLowLatency()) {
						int bitCount = bitCountByEdge.get(i);
						bitCount += add;
						if (first) {
							first = false;
							bitCount += excess % lowLatencyCount;
						}
						bitCountByEdge.set(i, bitCount);
					}
					i++;
				}
			}
		}

		int i = 0;
		List<P2PEdge> hasNeighbors = new ArrayList<>();
		for (AsyncRequestState<HasFile> req : onlySuccess) {
			InfoByEdge e = new InfoByEdge();
			int bitCount = bitCountByEdge.get(i);
			i++;
			e.setBitCountMax(bitCount);

			e.setEdge(req.getTo());
			e.setReq(req);

			HasFileResponse res = (HasFileResponse) req.getReq().getRes()
					.getContent();
			e.setWriteBits(WriteBits.deserializeStatic(res.getWriteBits()));

			ctx.getByEdge().add(e);
			hasNeighbors.add(req.getTo());
		}

		ctx.setHasNeighbors(new ReadonlyNeighborList(hasNeighbors));

		int maxBitCount = Collections.max(bitCountByEdge);
		//maxBitCountに応じてタイムアウト時間を決定する
		long dlTimeout = (long) maxBitCount * 1000 * 20;
		ctx.setDlTimeout(dlTimeout);

		if (!ctx.validateAtUse()) {
			Glb.getLogger().error("Failed to setup ctx",
					new IllegalStateException());
			return false;
		}

		return downloadSyncInternalFragmentation(ctx);
		/*
		//断片化が生じているか
		if (f.getNewWriteBits().getBits().isFragmentation()) {
			return downloadSyncInternalFragmentation(ctx);
		} else {
			return downloadSyncInternalSerial(ctx);
		}
		*/
	}

	/**
	 * ファイルの一部は既にDL済みである可能性がある。
	 * それはwriteBitsに表現されている。
	 * その時未DLの部分は断片化（writeBitsの不連続領域の増加、短縮）している可能性がある。
	 *
	 * できるだけ新たな断片化を抑えて、まとまった単位でリクエストしたい。
	 * 処理効率が上がる。
	 *
	 * できるだけ低レイテンシノードからDLしたい。
	 *
	 * @return	DLに成功したか
	 */
	private boolean downloadSyncInternalFragmentation(DownloadCtx ctx) {
		//低レイテンシノードはアクセスが増える一方でストレージ性能の高さは不明だから、
		//できるだけ連続アクセスにすべき。
		//連続アクセスならHDDでもかなりの性能が出る。

		//逆に高レイテンシノードに断片化した部分を担当させる。
		//断片化はCPUやストレージの負荷割合を高める一方で通信量の増加は限定的だから。

		//荷物（＝DLされるファイルの部分）一覧。担当が決まったら随時削除される。荷物の分割による追加もある
		//第二引数をfalseにすることでビットが0の部分のみ、つまり
		//まだ持っていない部分のみをloadとすることができる。
		List<BitZone> loads = ctx.getWriteBits().getBits().fragmentations(true,
				false);

		if (loads.size() == 0) {
			Glb.getLogger().warn("No loads", new IllegalStateException());
		}

		Glb.debug("serial part loads=" + loads);

		//まず各近傍ができるだけ連続した荷物を容量の限り選択する
		int loop = 0;
		for (InfoByEdge e : ctx.getByEdge()) {
			int loop2 = 0;
			while (drawAndAssign(e, loads) && loop2 < 2000) {
				loop2++;
			}
			if (loads.size() == 0) {
				break;
			}

			loop++;
			if (loop > 1000 * 100) {
				Glb.getLogger().warn("Abnormal loop", new Exception());
				break;
			}
		}

		if (loads.size() > 0) {
			Glb.debug("forcibly part loads=" + loads);
			//残っている荷物を順に担当するが、最大容量を無視する
			loop = 0;
			for (InfoByEdge e : ctx.getByEdge()) {
				int loop2 = 0;
				while (drawAndAssign(e, loads, true) && loop2 < 2000) {
					loop2++;
				}
				if (loads.size() == 0) {
					break;
				}

				loop++;
				if (loop > 1000 * 100) {
					Glb.getLogger().warn("Abnormal loop", new Exception());
					break;
				}
			}
		}

		//ここまでのアルゴリズムでは、容量制限のためにわざわざ分割して担当したのに
		//forcibly partで結局それに続く部分を担当して連続領域を分割して受信する場合がある
		//そこで連続しているBitZoneを連結する
		for (InfoByEdge e : ctx.getByEdge()) {
			BitZone.concat(e.getAssignedLoads());
		}

		return requestAndWrite(ctx);
	}

	/**
	 * 担当可能範囲drawerで対象荷物loadをeのcapacityの範囲で担当し、
	 * 対象荷物の残り部分は新たな荷物としてloadsに登録される。
	 *
	 * @param load
	 * @param drawer
	 * @param e
	 * @param loads
	 */
	private void draw(BitZone load, BitZone drawer, InfoByEdge e, int size,
			List<BitZone> loads) {
		BitZone assignedLoad = load.draw(drawer, size, loads);
		if (assignedLoad != null) {
			loads.remove(load);
			e.assign(assignedLoad);
		}
	}

	/**
	 * @param sl
	 * @param e
	 * @param loads
	 */
	private void draw(SelectedLoad sl, InfoByEdge e, int sizeLimit,
			List<BitZone> loads) {
		draw(sl.getLoad(), sl.getDrawer(), e, sizeLimit, loads);
	}

	/**
	 * この近傍が担当できるできるだけ連続した領域を
	 * 荷物一覧から見つけて担当させる。
	 *
	 * @param e		近傍
	 * @param loads	荷物一覧
	 */
	public boolean drawAndAssign(InfoByEdge e, List<BitZone> loads) {
		return drawAndAssign(e, loads, false);
	}

	public boolean drawAndAssign(InfoByEdge e, List<BitZone> loads,
			boolean forcibly) {
		SelectedLoad serialLoad = getMaxSerialLoad(e, loads);
		if (serialLoad == null) {
			return false;
		}
		//drawerのビットが立っている部分と荷物のビットが立っていない部分の一致を求めるので
		//drawerのビットを0にする
		//drawerは直前のメソッドで作成されているので他への影響はない
		serialLoad.getDrawer().setBit(false);

		if (forcibly) {
			draw(serialLoad, e, -1, loads);
		} else {
			draw(serialLoad, e, e.getCapacity(), loads);
		}
		return true;
	}

	/**
	 * @param e
	 * @param loads	この中から選ばれる
	 * @return	eが担当できる最も長く連続した部分
	 */
	private SelectedLoad getMaxSerialLoad(InfoByEdge e, List<BitZone> loads) {
		Bits otherWbBits = e.getWriteBits().getBits();
		List<BitZone> otherBitZones = otherWbBits.fragmentations(true, true);

		//連続した最も大きな部分を担当できる荷物を探す
		int maxBitCount = -1;
		BitZone serialLoad = null;
		BitZone drawer = null;
		for (BitZone currentLoad : loads) {
			for (BitZone otherBitZone : otherBitZones) {
				int thisBitCount = otherBitZone
						.getSameCountInverted(currentLoad);
				if (thisBitCount > maxBitCount) {
					maxBitCount = thisBitCount;
					serialLoad = currentLoad;
					drawer = otherBitZone;
				}
			}
		}

		if (drawer == null || serialLoad == null)
			return null;

		SelectedLoad r = new SelectedLoad();
		r.setDrawer(drawer);
		r.setLoad(serialLoad);
		r.setBitCount(maxBitCount);
		return r;
	}

	/**
	 * 定期削除。3か月以上アクセスが無い動的ファイルを削除する。
	 *
	 * 3か月以上アクセスが無い事は近傍が必要としていない事を意味するので、
	 * 自分の分担範囲のファイルでも削除する。
	 * ただし自分がアップロードしたファイルは削除されない。
	 *
	 * この仕様は、ネットワークからファイルが喪失される可能性があることを意味する。
	 * もし喪失されたら、アップロード者は十分なHDD容量を備えた上で
	 * GUI操作を通じて再度ファイルを設置する事でファイルを再度ネットワークに流せる。
	 */
	private void periodicDelete() {
		FileManagement m = Glb.getFile();
		//このフォルダ以下のみ定期削除の対象とする
		Path root = m.getPath(m.getDynamicFileDir());
		File d = root.toFile();
		if (!d.isDirectory()) {
			return;
		}

		//3か月
		long threshold = 1000L * 60 * 60 * 24 * 30 * 3;

		try {
			Files.walk(root).filter(Files::isRegularFile).forEach(f -> {
				try {
					if (canDelete(f, threshold)) {
						if (!canDelete(f, root)) {
							return;
						}

						Glb.getLogger().info("periodicDelete file=" + f);
						if (!m.remove(f)) {
							Glb.getLogger().warn("Failed to remove file=" + f,
									new Exception());
						}
					}
				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
			});
			Files.walk(root).filter(Files::isDirectory).forEach(f -> {
				try {
					if (canDelete(f, threshold)) {
						//もしフォルダが空なら削除する
						deleteEmptyDir(f, root);
					}
				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
			});
		} catch (IOException e) {
			Glb.getLogger().error("", e);
		}
	}

	private boolean canDelete(Path f, long threshold) throws IOException {
		BasicFileAttributes attrs = Files.readAttributes(f,
				BasicFileAttributes.class);
		FileTime readT = attrs.lastAccessTime();
		long readTmillis = readT.toMillis();
		FileTime creT = attrs.creationTime();
		long creTmillis = creT.toMillis();
		long t = readTmillis > creTmillis ? readTmillis : creTmillis;

		if (t < 1000) {
			return false;//正しく時間を取得できていない場合
		}

		long dif = (System.currentTimeMillis() - t);
		return dif > threshold;

	}

	private boolean canDelete(Path p, Path stop) {
		if (p == null || stop == null || p.toString().length() == 0
				|| stop.toString().length() == 0
				|| !Glb.getFile().isAppPath(p)) {
			Glb.getLogger().warn("invalid path=" + p + " stop=" + stop,
					new Exception());
			return false;
		}

		String stopStr = stop.toAbsolutePath().toString();
		String dirStr = p.toAbsolutePath().toString();
		if (stopStr == null || dirStr == null || !dirStr.startsWith(stopStr)) {
			Glb.getLogger().warn("invalid path=" + p + " stop=" + stop,
					new Exception());
			return false;
		}

		if (dirStr.equals(stopStr)) {
			return false;
		}

		return true;
	}

	/**
	 * @param dir	削除対象のディレクトリ
	 * @param stop	パスがこれと一致した場合削除しない
	 * @return	nullまたは削除された場合削除されたディレクトリの親ディレクトリ
	 */
	private Path deleteEmptyDir(Path dir, Path stop) {
		if (!canDelete(dir, stop))
			return null;

		File paref = dir.toFile();
		if (paref != null && !stop.equals(dir) && paref.isDirectory()
				&& paref.list().length == 0) {
			Glb.getLogger().info("periodicDelete file=" + dir);
			if (!Glb.getFile().remove(dir)) {
				Glb.getLogger().warn("Failed to remove dir=" + dir,
						new Exception());
			}

			return dir;
		}
		return null;
	}

	/**
	 * 定期DL。DLメソッドの引数のnecessaryフラグは
	 * 基盤ソフトウェアを構成するファイルはtrueそれ以外はfalseで呼び出す。
	 * それ以外のファイルがnecessary=trueでDLされるのは使用直前
	 * false設定でできるだけDLするという動作はネットワーク全体に徐々に
	 * ファイルを広げておく効果がある。
	 */
	private void periodicDownload() {
		periodicDownloadPlatform();
		//基盤ソフトウェアとの処理優先度の違いを表現するため
		Glb.getExecutorSlow().submit(() -> {
			//TODO
			//periodicDownloadMaterial();
			periodicDownloadRatingGame();
			periodicDownloadStaticGame();
		});
	}

	/**
	 * ラムダのためローカル変数を更新できるように
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	private static class Local {
		public long id;
	}

	/**
	 * 素材DL
	 */
	/*
	private void periodicDownloadMaterial() {
		periodicDownloadCommon((Material m) -> {
			List<TenyutalkFileMetadataI> l = new ArrayList<>();
			l.add(m.getFile());
			return l;
		}, Glb.tryW(txn -> new MaterialStore(txn), null));
	}
	*/

	private <I extends ModelI, O extends I> void periodicDownloadCommon(
			Function<O, List<TenyutalkFileMetadataI>> func,
			Function<Transaction, ModelStore<I, O>> getStore) {
		Local local = new Local();
		local.id = ModelI.getFirstId();
		int dbLoop = 1000;
		long sleep = 200;
		while (true) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
			}
			//どう書くべきか微妙だったが、同じトランザクションで長時間処理するより
			//定期的に新しいトランザクションに変えた方が良いかと思った。
			//DBに長時間トランザクションにまつわる問題が何かあるかもしれないから。
			//あるいは単に、その処理をしている間にDBが更新された場合、
			//DL処理が無駄になる。
			boolean ret = Glb.getObje().readRet(txn -> {
				try {
					ModelStore<I, O> s = getStore.apply(txn);
					Long lastId = s.getLastId();
					for (int i = 0; i < dbLoop; i++) {
						if (local.id > lastId)
							return true;
						O m = s.get(local.id++);
						if (m == null) {
							continue;
						}
						List<TenyutalkFileMetadataI> files = func.apply(m);
						for (TenyutalkFileMetadataI f : files) {
							ValidationResult vr = new ValidationResult();
							if (!f.validateAtCreate(vr)) {
								Glb.getLogger().error("" + vr, new Exception());
								continue;
							}
							downloadSync(f, false);
						}
					}
				} catch (Exception e) {
					Glb.getLogger().error("", e);
				}
				return false;
			});
			if (ret) {
				return;
			}
		}
	}

	/**
	 * 基盤ソフトウェアDL
	 */
	private void periodicDownloadPlatform() {
		//次のバージョンのファイル情報があるか
		//現在のバージョン
		int currentRelease = Glb.getConst().getRelease();
		//現在または次のバージョン
		TenyuPlatformSoftware plat = Glb.getObje().getCore()
				.getLatestAcceptedPlatformSoftware();
		if (plat == null) {
			//最初のバージョンでは議題で可決されたソフトウェア情報が無い
			//それ以外のバージョンでは必ずある
			if (currentRelease != 1) {
				Glb.getLogger().error(
						"getLatestAcceptedPlatformSoftware is null",
						new Exception());
			}
			return;
		}

		//現在実行されているソフトウェアのバージョンと一致していたらやる事は無い
		if (plat.getRelease() == currentRelease) {
			return;
		}

		//DL試行。結果を確認しない。定期的に繰り返され、バージョンアップ直前に再度DLが試行される。
		//既にDL済みかもしれないが、その場合DL処理は内部で行われない
		downloadAsync(plat.getFiles(), true);
	}

	/**
	 * レーティングゲーム
	 */
	private void periodicDownloadRatingGame() {
		//		periodicDownloadCommon((RatingGame o) -> o.getClientFiles(),
		//				Glb.tryW(txn -> new RatingGameStore(txn), null));
	}

	/**
	 * 常駐空間ゲーム
	 */
	private void periodicDownloadStaticGame() {
		//		periodicDownloadCommon((StaticGame o) -> o.getClientFiles(),
		//				Glb.tryW(txn -> new StaticGameStore(txn), null));
	}

	/**
	 * 通信とwriteBitsの書き込み
	 * @param ctx
	 * @return
	 */
	private boolean requestAndWrite(DownloadCtx ctx) {
		AsyncRequestStatesNoRetry<
				GetFile> dlStates = new AsyncRequestStatesNoRetry<>(
						() -> ctx.getHasNeighbors());

		//分散DL開始
		for (InfoByEdge e : ctx.getByEdge()) {
			for (BitZone load : e.getAssignedLoads()) {
				AsyncRequestState<HasFile> has = e.getReq();
				P2PEdge to = e.getEdge();
				if (to == null || has == null)
					continue;
				//近傍毎に異なるリクエストを送信する事と
				//currentBitIndexという外部状態を更新する必要があるので
				//AsyncRequestStatesNoRetry側のメソッドとして実装できなかった
				GetFile req = new GetFile();
				req.setPosition(load.getStartIndex() * WriteBits.unit);
				req.setSize(load.size() * WriteBits.unit);
				req.setFile(ctx.getFile());

				RequestFutureP2PEdge dlState = Glb.getP2p()
						.requestFileAsync(req, to);
				dlStates.add(new AsyncRequestState<GetFile>(dlState, req, to));
			}
		}

		//全リクエストの終了を待機
		dlStates.waitAllDone(ctx.getDlTimeout());

		if (ctx.getFile().validateFile()) {
			//全て成功
			ctx.getFile().deleteWriteBits();
			return true;
		} else {
			//未DL部分が存在している
			//原因として通信失敗、そもそもその部分を持っているノードが居ない等

			//成功したところについてwriteBitを立てる
			for (int requestIndex = 0; requestIndex < dlStates.getRequests()
					.size(); requestIndex++) {
				AsyncRequestState<GetFile> getFile = dlStates.getRequests()
						.get(requestIndex);
				if (getFile.getState().isSuccess()) {
					int bitCountThisEdge = (int) (getFile.getReq().getSize()
							/ WriteBits.unit);
					int startBitIndex = (int) (getFile.getReq().getPosition()
							/ WriteBits.unit);
					//1リクエスト中では連続した領域が問い合わされるのでこの処理で良い
					for (int currentBitIndex = startBitIndex; currentBitIndex < bitCountThisEdge
							+ startBitIndex; currentBitIndex++) {
						ctx.getWriteBits().getBits().stand(currentBitIndex);
					}
				}
			}

			//writeBitsが全て立っているか
			if (ctx.getWriteBits().getBits().isFilledOne()) {
				//それなのに検証に成功していないので、ファイルとwriteBitsを削除する。
				ctx.getFile().delete();
				Glb.debug(
						"Failed to validate. and all bits is one. file is deleted.");
			} else {
				//一部が成功しただけなので全体の整合性検証を通過しなかった
				//writeBitsをファイルに書き込む
				if (!ctx.getFile().writeWriteBits(ctx.getWriteBits())) {
					Glb.getLogger().error("Failed to write writeBits ",
							new Exception());
				}
			}

			Glb.getLogger().info("download not completed path="
					+ ctx.getFile().getRelativePathStr());

			return false;
		}
	}

	public static long firstSleep = 1000L * 20;
	public static long secondSleep = 1000L * 60 * 20;

	@Override
	public void start() {
		GlbMemberDynamicState.super.start();
		long initialWait = firstSleep;
		if (periodic != null) {
			periodic.cancel(false);
			initialWait += secondSleep;
		}
		periodic = Glb.getExecutorPeriodic().scheduleAtFixedRate(() -> {
			try {
				periodicDownload();
				periodicDelete();
			} catch (Exception e) {
				Glb.getLogger().error("", e);
			}
		}, initialWait, secondSleep, TimeUnit.MILLISECONDS);
	}

	@Override
	public void stop() {
		if (periodic != null && !periodic.isCancelled()) {
			periodic.cancel(false);
		}
		GlbMemberDynamicState.super.stop();
	}

	/**
	 * リストが複数あるがneighborsの並びに従って同じインデックスなら
	 * 同じオブジェクト
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	@SuppressWarnings("unused")
	private static class DownloadCtx {
		/**
		 * 近傍毎の情報
		 * レイテンシソートされている
		 */
		private List<InfoByEdge> byEdge = new ArrayList<>();
		/**
		 * 全リクエスト共通のタイムアウト時間
		 */
		private long dlTimeout;
		/**
		 * DL対象ファイル
		 */
		private TenyutalkFileMetadataI file;

		/**
		 * ファイルの少なくとも一部を持っていた近傍一覧
		 */
		private ReadonlyNeighborList hasNeighbors;

		/**
		 * 最初の問い合わせ先
		 * このうち返信を返したものだけが後の処理に影響する
		 */
		private ReadonlyNeighborList initNeighbors;

		/**
		 * まだDLされていないwriteBits数
		 */
		private int notDownloadedBitCount;
		/**
		 * DLされた部分とされていない部分を区別する情報
		 */
		private WriteBits writeBits;

		/**
		 * @return	DL中の仮パス。
		 */
		public Path getDownloadingPath() {
			return Paths.get(file.getRelativePathStr(), ".downloading");
		}

		/**
		 * DL完了後に呼び出し、仮パスを正しいパスにリネーム
		 * @return	リネームに成功したか
		 */
		public boolean renameDownloadingPath() {
			Path tmp = getDownloadingPath();
			if (!tmp.toFile().exists())
				return false;
			return tmp.toFile().renameTo(file.getRelativePath().toFile());
		}

		public List<InfoByEdge> getByEdge() {
			return byEdge;
		}

		public long getDlTimeout() {
			return dlTimeout;
		}

		public TenyutalkFileMetadataI getFile() {
			return file;
		}

		public ReadonlyNeighborList getHasNeighbors() {
			return hasNeighbors;
		}

		public ReadonlyNeighborList getInitNeighbors() {
			return initNeighbors;
		}

		public int getNotDownloadedBitCount() {
			return notDownloadedBitCount;
		}

		public WriteBits getWriteBits() {
			return writeBits;
		}

		public void setByEdge(List<InfoByEdge> byEdge) {
			this.byEdge = byEdge;
		}

		public void setDlTimeout(long dlTimeout) {
			this.dlTimeout = dlTimeout;
		}

		public void setFile(TenyutalkFileMetadataI file) {
			this.file = file;
		}

		public void setHasNeighbors(ReadonlyNeighborList hasNeighbors) {
			this.hasNeighbors = hasNeighbors;
		}

		public void setInitNeighbors(ReadonlyNeighborList initNeighbors) {
			this.initNeighbors = initNeighbors;
		}

		public void setNotDownloadedBitCount(int notDownloadedBitCount) {
			this.notDownloadedBitCount = notDownloadedBitCount;
		}

		public void setWriteBits(WriteBits writeBits) {
			this.writeBits = writeBits;
		}

		public int size() {
			return byEdge.size();
		}

		public boolean validateAtUse() {
			boolean b = true;
			if (byEdge == null || byEdge.size() == 0) {
				b = false;
			}
			if (dlTimeout <= 0) {
				b = false;
			}
			if (file == null) {
				b = false;
			}
			if (hasNeighbors == null || hasNeighbors.size() == 0) {
				b = false;
			}
			if (initNeighbors == null || initNeighbors.size() == 0) {
				b = false;
			}
			if (notDownloadedBitCount <= 0) {
				b = false;
			}
			if (writeBits == null) {
				b = false;
			}

			if (b) {
				for (InfoByEdge e : byEdge) {
					if (!e.validate()) {
						b = false;
					}
				}
			}

			return b;
		}
	}

	@SuppressWarnings("unused")
	private static class InfoByEdge {
		/**
		 * 担当する荷物
		 */
		private List<BitZone> assignedLoads = new ArrayList<>();

		/**
		 * 現在までに担当した荷物の量
		 */
		private int bitCountLoad;
		/**
		 * 最大容量
		 * ただし超えても大した問題は無いので目安として捉える
		 */
		private int bitCountMax;
		private P2PEdge edge;
		private AsyncRequestState<HasFile> req;
		private WriteBits writeBits;

		public void addBitCount(int add) {
			bitCountLoad += add;
		}

		public void addBitCountMax(int add) {
			bitCountMax += add;
		}

		/**
		 * 荷物を担当する
		 * 大きな荷物は分割して分割後のBitZoneを渡す
		 * @param load
		 * @param e
		 */
		private void assign(BitZone load) {
			if (load == null || load.size() == 0)
				return;
			addBitCount(load.size());
			getAssignedLoads().add(load);

			Glb.debug("assigned load=" + load + " edge=" + this);
		}

		public List<BitZone> getAssignedLoads() {
			return assignedLoads;
		}

		public int getBitCountLoad() {
			return bitCountLoad;
		}

		public int getBitCountMax() {
			return bitCountMax;
		}

		public int getCapacity() {
			return bitCountMax - bitCountLoad;
		}

		public P2PEdge getEdge() {
			return edge;
		}

		public AsyncRequestState<HasFile> getReq() {
			return req;
		}

		public WriteBits getWriteBits() {
			return writeBits;
		}

		public void setAssignedLoads(List<BitZone> assignedLoads) {
			this.assignedLoads = assignedLoads;
		}

		public void setBitCountLoad(int bitCountLoad) {
			this.bitCountLoad = bitCountLoad;
		}

		public void setBitCountMax(int bitCountMax) {
			this.bitCountMax = bitCountMax;
		}

		public void setEdge(P2PEdge edge) {
			this.edge = edge;
		}

		public void setReq(AsyncRequestState<HasFile> req) {
			this.req = req;
		}

		public void setWriteBits(WriteBits writeBits) {
			this.writeBits = writeBits;
		}

		@Override
		public String toString() {
			return edge.toString() + " writeBits=" + writeBits;
		}

		public boolean validate() {
			boolean b = true;
			if (assignedLoads == null) {
				b = false;
			}
			if (bitCountLoad < 0) {
				b = false;
			}
			if (edge == null) {
				b = false;
			}
			if (bitCountMax < 0) {
				b = false;
			}
			if (req == null) {
				b = false;
			}
			if (writeBits == null) {
				b = false;
			}
			return b;
		}

	}

	/**
	 * 担当可能範囲によって選択された部分
	 *
	 * @author exceptiontenyu@gmail.com
	 *
	 */
	@SuppressWarnings("unused")
	private static class SelectedLoad {
		/**
		 * drawerとloadの重複ビット数
		 */
		private int bitCount;
		/**
		 * loadと重複したdrawer
		 * あるいは、荷物を該当させた担当部分
		 */
		private BitZone drawer;
		private BitZone load;

		public int getBitCount() {
			return bitCount;
		}

		public BitZone getDrawer() {
			return drawer;
		}

		public BitZone getLoad() {
			return load;
		}

		public void setBitCount(int bitCount) {
			this.bitCount = bitCount;
		}

		public void setDrawer(BitZone drawer) {
			this.drawer = drawer;
		}

		public void setLoad(BitZone load) {
			this.load = load;
		}

	}

}
