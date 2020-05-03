package bei7473p5254d69jcuat.tenyu.communication.mutual.right;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import glb.*;
import glb.util.*;
import glb.util.Util.*;
import jetbrains.exodus.env.*;

/**
 * 客観を更新する。各ユーザーが自分のタイミングでP2Pネットワーク全体で
 * 共有されたデータを更新できる。ただし更新できるのはそのユーザーの権限で認められた
 * 範囲だけ。メッセージは受付サーバに集められてMessageListを作り、
 * MessageListが各ノードに送信され一斉に反映される。
 *
 * 客観の更新は自由更新や分散合意によっても行われる。
 * その整合性は更新箇所の違いやタイミングの調整によって達成される。
 *
 * 非スレッドセーフ
 *
 * @author exceptiontenyu@gmail.com
 */
public class UserMessageList implements StorableI{
	/**
	 * 現在の想定される反映時負荷量。
	 * メッセージ件数と一致しない。loadSize()の合計
	 */
	private int applySizeTotal = 0;

	/**
	 * TODO 廃止予定
	 * このメッセージリストを反映する直前のヒストリーインデックス
	 * このメッセージリストが反映されるとヒストリーインデックスはこの数値+1になる
	 */
	private long historyIndex = -1;

	/**
	 * メッセージ一覧
	 * メッセージの内容はUserMessageListRequestIの実装が必要
	 */
	private List<Message> messages = new ArrayList<>();
	//	private List<ManagerPackage> managerMessages = new ArrayList<>();

	/**
	 * データサイズ
	 */
	private int sizeTotal = 0;

	/**
	 * 反映できないメッセージを除去済みか。
	 *
	 * メッセージリスト内の整合性において検証されただけで、
	 * DBとの整合性などはチェックされていないので、trueでもなお
	 * 一部のメッセージは反映されない可能性がある。
	 */
	private transient boolean validated = false;

	/**
	 * メッセージリストにメッセージを登録する。
	 * サイズチェックがあるので必ずこのメソッドを通じて追加されなければならない。
	 * @param c	検証済みユーザーメッセージ
	 * @return	登録に成功したか
	 */
	public boolean add(Message validatedUserMessage) {
		//梱包と内容が必要なインターフェースを実装しているか
		if (!(validatedUserMessage.getInnermostPack() instanceof SignedPackage)
				|| !(validatedUserMessage
						.getContent() instanceof UserMessageListRequestI))
			return false;

		LoadSetting setting = Glb.getObje().getCore().getConfig()
				.getLoadSetting();

		//件数チェック
		/*	反映負荷量と容量で必要な検証ができている。
		if (messages.size() >= setting.getUserMessageListCountMax())
			return false;
		*/

		//容量チェック
		long size = validatedUserMessage.getSize();
		if (size <= 0
				|| size + sizeTotal
						- LoadSetting.getToleranceSizeSerialize() >= setting
								.getUserMessageListSizeMax()
				|| size > setting.getMessageSizeMax()) {
			return false;
		}

		//反映負荷量チェック
		int apply = validatedUserMessage.getContent().getApplySize();
		if (apply + applySizeTotal > setting.getUserMessageListApplySizeMax())
			return false;

		if (!messages.add(validatedUserMessage))
			return false;
		sizeTotal += size;
		applySizeTotal += apply;
		return true;
	}

	/**
	 * 連結する
	 * @param other
	 */
	public void addAll(UserMessageList other) {
		for (Message m : other.messages) {
			//基本的な検証
			if (!m.validateAndSetup()) {
				continue;
			}

			//P2PDefenseの重複チェックに記録する必要がある
			//一応重複していたら無視するが、チェック処理はここで主眼ではない
			if (Glb.getP2pDefense().isDup(m))
				continue;

			add(m);
		}
	}

	public int getApplySizeTotal() {
		return applySizeTotal;
	}

	public long getHistoryIndex() {
		return historyIndex;
	}

	public long getNextHistoryIndex() {
		return historyIndex + 1;
	}

	public List<Message> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	/**
	 * 客観の更新履歴の一歩という意味でstep
	 */
	public List<Message> getSteps() {
		return messages;
	}

	/**
	 * @return	このオブジェクトのハッシュ値
	 */
	public synchronized byte[] hash() {
		return Glb.getUtil().hashSecure(this);
	}

	public boolean isValidated() {
		return validated;
	}

	public void setHistoryIndex(long historyIndex) {
		this.historyIndex = historyIndex;
	}

	public void setSteps(List<Message> steps) {
		this.messages = steps;
	}

	/**
	 * @return	メッセージリストの件数
	 */
	public int size() {
		return messages.size();
	}

	private transient HashSet<ByteArrayWrapper> dup;
	private transient List<Message> removeList;

	private boolean addToRemoveList(Message m) {
		synchronized (removeList) {
			return removeList.add(m);
		}
	}

	private boolean isDup(ByteArrayWrapper e) {
		synchronized (dup) {
			if (dup.contains(e)) {
				return true;
			} else {
				dup.add(e);
				return false;
			}
		}
	}

	/**
	 * 反映できないメッセージの除去。
	 * ヒストリーインデックスのチェックも行われる。
	 * validatedフラグが設定される。
	 *
	 * 検証処理が重くメッセージスループットのボトルネックになるので
	 * 並列処理で解決する。コア数が増えれば解決する。
	 *
	 * @return 客観に反映可能か
	 */
	public synchronized final boolean validateAndRemove() {
		try {
			int messagesSize = messages.size();
			if (messagesSize == 0)
				return true;

			LoadSetting setting = Glb.getObje().getCore().getConfig()
					.getLoadSetting();

			long end = Glb.getObje().getCore().getHistoryIndex();
			long start = end
					- setting.getUserMessageListHistoryIndexTolerance();
			if (historyIndex < start || historyIndex > end)
				return false;

			//メッセージの重複チェック用
			dup = new HashSet<>();

			//1メッセージリスト中の1ユーザー当たりのメッセージ件数が制限される
			//user : message count
			Map<Long, Integer> messageCount = new HashMap<>();

			//ここに追加されたものは削除される
			removeList = new ArrayList<>();

			int coreNum = Glb.getConf().getPhysicalCoreNumber();
			if (coreNum == 0)
				coreNum = 1;
			//1コアあたりの処理件数
			int perCore = messagesSize / coreNum;
			int surplus = messagesSize % coreNum;

			ExecutorService executor = Executors.newFixedThreadPool(coreNum);
			for (int coreIndex = 0; coreIndex < coreNum; coreIndex++) {
				int coreIndexTmp = coreIndex;
				int perCoreTmp = perCore + (coreIndex == 0 ? surplus : 0);

				executor.execute(() -> {
					int startIndex = coreIndexTmp * perCoreTmp;
					for (int i = startIndex; i < startIndex + perCoreTmp; i++) {
						Message m = messages.get(i);

						//署名と内容が不正なら除去
						if (!m.validateAndSetup()) {
							Glb.debug(new Exception(
									"Failed to validateAndSetup()"));
							addToRemoveList(m);
							continue;
						}

						//内容が無いまたは適切なクラスでなければ除去
						//validateAndSetupの後じゃないと動作しない
						if (m.getContent() == null || !(m
								.getContent() instanceof UserMessageListRequestI)) {
							Glb.debug("content is null");
							addToRemoveList(m);
							continue;
						}

						//重複していたら除去
						if (isDup(new ByteArrayWrapper(
								m.getInnermostPack().getContentBinary()))) {
							Glb.debug("dup");
							addToRemoveList(m);
							continue;
						}

						//ユーザー毎の同時最大同時反映件数のチェック
						Long userId = m.getUserId();
						//これまでに許可されたメッセージ件数
						Integer count = messageCount.get(userId);
						if (count == null) {
							count = 0;
						}
						//1メッセージリスト中の最大メッセージ数
						long countMax = 0;
						if (setting == null) {
							countMax = 5;
						} else if (Glb.getObje().getCore()
								.isHeavyLoadServerToUserMessageListServer(
										userId)) {
							//サーバー
							countMax = setting.getServerCountMax();
						} else if (Glb.getConst().getAuthor().getId()
								.equals(userId)) {
							//作者
							countMax = setting.getAuthorCountMax();
						} else if (Glb.getObje().getCore().getManagerList()
								.isManager(userId)) {
							//運営
							countMax = setting.getAdminCountMax();
						} else {
							countMax = setting
									.getUserMessageListCountMaxStandard();
						}

						if (count >= countMax) {
							Glb.debug("count over");
							addToRemoveList(m);
							continue;
						} else {
							count++;
							messageCount.put(userId, count);
						}
					}
				});
			}

			//新規受付停止
			executor.shutdown();
			//完了待機
			try {
				executor.awaitTermination(1000L * 60, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Glb.getLogger().info("", e);
			}

			//除去リストに追加されたメッセージを除去する
			if (messages.removeAll(removeList)) {
				Glb.getLogger().info("removed. " + removeList.size());
			}

			validated = true;
			return true;
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			validated = false;
			return false;
		}
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		boolean b = true;
		if (messages == null) {
			r.add(Lang.USERMESSAGELIST_MESSAGES, Lang.ERROR_EMPTY);
			b = false;
		} else {
			if (!validated) {
				if (!validateAndRemove()) {
					r.add(Lang.USERMESSAGELIST_MESSAGES, Lang.ERROR_INVALID);
					b = false;
				}
			}
		}

		if (historyIndex < ObjectivityCore.firstHistoryIndex) {
			r.add(Lang.USERMESSAGELIST_HISTORYINDEX, Lang.ERROR_TOO_LITTLE);
			b = false;
		}

		if (applySizeTotal < 0) {
			r.add(Lang.USERMESSAGELIST_APPLYSIZETOTAL, Lang.ERROR_TOO_LITTLE);
			b = false;
		}

		if (sizeTotal < 0) {
			r.add(Lang.USERMESSAGELIST_SIZETOTAL, Lang.ERROR_TOO_LITTLE);
			b = false;
		}

		return b;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		return validateAtCreate(r);
	}

	@Override
	public boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + applySizeTotal;
		result = prime * result + (int) (historyIndex ^ (historyIndex >>> 32));
		result = prime * result
				+ ((messages == null) ? 0 : messages.hashCode());
		result = prime * result + sizeTotal;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserMessageList other = (UserMessageList) obj;
		if (applySizeTotal != other.applySizeTotal)
			return false;
		if (historyIndex != other.historyIndex)
			return false;
		if (messages == null) {
			if (other.messages != null)
				return false;
		} else if (!messages.equals(other.messages))
			return false;
		if (sizeTotal != other.sizeTotal)
			return false;
		return true;
	}

}
