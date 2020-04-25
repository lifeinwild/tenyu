package bei7473p5254d69jcuat.tenyu.communication.mutual.processorprovement;

import java.nio.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

import org.codehaus.commons.compiler.*;
import org.codehaus.janino.*;

import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import glb.Conf.*;
import glb.util.Util.*;

/**
 * CPU証明。TODO:本番動作での適切な負荷調節、ハードウェアアクセラレーション防止のための
 * 計算の複雑化
 *
 * プロセッサ証明に関する大抵のコードはここに書かれている。
 * マルチスレッドで処理して演算量を稼ぐインターフェースがある。
 * プロセッサ証明に関してはReadme参照。
 *
 * @author exceptiontenyu@gmail.com
 */
public strictfp class CPUProvement {
	private MessageDigest md;
	private SimpleCompiler compiler;
	private final String packageStr = "sample";
	private final String[] lOpe2 = { "+", "-", "/", "*", "|", "&", "^", "%" },
			dOpe2 = { "+", "-", "/", "*" };
	private volatile String[] lVar = { "l1", "l2", "l3", "l4", "l5", "l6", "l7",
			"l8", "l9", "l10" },
			dVar = { "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9",
					"d10" };
	private String loop;
	private String recursiveLoop = Glb.getConf().getRunlevel()
			.equals(RunLevel.DEV) ? "2" : "3";
	private final String lInit = "123456789L", dInit = "3.141592D";
	private final String lVarStr = "lVar", dVarStr = "dVar";
	private final int ope1max = 2, ope2max = 2;

	/**
	 * 回答作成にかかる最長時間。しかし、厳密にこの時間以内に計算が終わる
	 * わけではない。概ねこの時間以内に終わる。
	 * なおプロセッサ証明の時間がnとすればn/3くらいであるべきである。
	 */
	private static final long computeTimeMax = Glb.getConf().getRunlevel()
			.equals(RunLevel.DEV) ? 1000L * 5 : 1000L * 40;

	public static long getComputeTimeMax() {
		return computeTimeMax;
	}

	/**
	 * 問題関数の引数探索の出力値は頭にいくつか指定の値があるかを条件にするが、そのバイト数。
	 * その０の数。1なら検証者は回答者の2^8分の1の負荷で検証できる。2なら2^16分の1
	 */
	private final int outputRestrictCount = 2;
	/**
	 * 問題関数の出力の頭はこの値でなければならない
	 * 0や1は計算上の特異点になりうるので避けるべき
	 */
	private final int outputRestrictValue = 100;

	/**
	 * 問題関数の内部関数のサイズを調節できる。
	 * Javaは1メソッドの最大サイズが64kb。
	 */
	private final int internalFuncSize = Glb.getConf().getRunlevel()
			.equals(RunLevel.DEV) ? 2 : 25;

	/**
	 * 問題関数の内部関数の再起呼び出しのネストの深さ
	 */
	private final int recursiveMax = Glb.getConf().getRunlevel()
			.equals(RunLevel.DEV) ? 2 : 3;

	/**
	 * 問題関数の内部関数の数
	 */
	private final int internalFuncCount = Glb.getConf().getRunlevel()
			.equals(RunLevel.DEV) ? 2 : 3;

	private final String crlf = System.getProperty("line.separator");

	private String className;
	private String code;

	private ProblemSrc p;
	private Solve solved;
	private Solve answer;

	private boolean verifyMode = false;

	private DynamicProblem dp;

	/**
	 * @return	検証1回あたりの平均時間
	 */
	public static long getVerifytime() {
		return 2;//2msになるように問題関数や引数探索を調節する
	}

	/**
	 * 計算のためのリセット。問題作成はコンストラクタだけなのでリセットされない。
	 */
	private void reset() {
		verifyMode = false;
		solved = null;
		answer = null;
		md.reset();
	}

	public CPUProvement(String rndStr, int year, int month, int day, int hour,
			int parallelNumber, byte[] p2pNodeId) throws Exception {
		ProblemSrc p = new ProblemSrc(rndStr, year, month, day, hour,
				parallelNumber, p2pNodeId);
		this.p = p;
		setup();
	}

	public CPUProvement(ProblemSrc p) throws Exception {
		this.p = p;
		setup();
	}

	/**
	 * srcのうちJavaのクラス名として可能な文字だけsbに追記する
	 * @param sb
	 * @param src
	 */
	private void appendJavaStr(StringBuilder sb, String src) {
		for (char c : src.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)) {
				sb.append(c);
			}
		}
	}

	public static final MessageDigest getMD() throws NoSuchAlgorithmException {
		return MessageDigest
				.getInstance(Glb.getConst().getDigestAlgorithmSecure());
	}

	private void setup() throws Exception {
		long start = System.currentTimeMillis();

		//問題関数のループ回数。繰り返し処理はCPUの機能の一つであり、
		//アクセラレーション防止のためある程度の回数にすべき。
		//他のプロセッサでも繰り返し処理はあるので多くする必要はない。
		switch (Glb.getConf().getRunlevel()) {
		case RELEASE:
		case TEST:
			loop = "10";
			break;
		case DEV:
		default:
			loop = "2";
			break;
		}
		//ハッシュ作成オブジェクト
		md = getMD();

		//コンパイラ関係
		//Janinoによるオンメモリコンパイル。
		//Java9からtools.jarが削除されたのでJDKから変更
		compiler = new SimpleCompiler();
		compiler.setCompileErrorHandler(new ErrorHandler() {
			@Override
			public void handleError(String message, Location optionalLocation)
					throws CompileException {
				Glb.getLogger().error(
						message + "Location:" + optionalLocation.toString());
			}
		});

		//動的作成されるクラス名の末尾
		StringBuilder suffix = new StringBuilder();
		//多数の近傍ノードが問題を送信してくる事、1ノードでも多数の問題を解く事、
		//1日3回ずっと問題を解くことを考えると、クラス名の重複を避ける必要がある。
		//大量の動的クラスが作成されるからである
		byte[] h = p.createHash(md);
		String classNameSrc = Base64.getEncoder().encodeToString(h);
		appendJavaStr(suffix, "_" + classNameSrc);
		className = "Problem" + suffix;

		Glb.debug(() -> className);

		//ハッシュから動的コードを作成
		code = generateProblem(h, className);

		//Glb.debug(code);

		//コンパイラ用タスク作成
		compiler.cook(code);

		//動的プログラムのロード
		Class<?> heaped = compiler.getClassLoader()
				.loadClass(packageStr + "." + className);
		if (heaped == null) {
			Glb.getLogger().error("failure");
			return;
		}
		Object o = heaped.getDeclaredConstructor().newInstance();
		if (o == null || !(o instanceof DynamicProblem)) {
			Glb.getLogger().error("failure");
			return;
		}
		dp = (DynamicProblem) o;

		long end = System.currentTimeMillis();
		Glb.debug(() -> "作成: " + (end - start) + "ms");
	}

	/**
	 * 動的な問題関数の作成。
	 * 対象配列があり、そのハッシュ値が出力になるので、
	 * 普遍的に、つまりあらゆる問題作成情報で、
	 * 解が特定の区間に密集するという事は無い。
	 * 特定の問題作成情報に限定すればあるかもしれない。
	 * 各演算の性質によってある引数候補を調べると別の引数候補を
	 * 調べなくていいということは普遍的にあるかもしれない。
	 * それは出力が同じになる場合である。
	 * しかし問題関数の作成方法自体随時変更していくつもりなので、
	 * 厳密に問題関数の性質を考えるコストをかけるべきと思わない。
	 */
	public String generateProblem(byte[] h, String cN) {
		byte[][] hashes = new byte[internalFuncCount][h.length];
		for (int i = 0; i < internalFuncCount; i++) {
			if (i == 0) {
				hashes[i] = md.digest(h);
			} else {
				hashes[i] = md.digest(hashes[i - 1]);
			}
		}

		byte[] h2 = new byte[h.length];
		for (int i = 0; i < h.length; i++) {
			h2[i] = h[h.length - i - 1];
		}

		String r = "";

		r += "package " + packageStr + ";" + crlf;
		r += "import " + CPUProvement.class.getPackage().getName() + ".*;"
				+ crlf;
		r += "import java.security.MessageDigest;" + crlf;
		r += "import java.nio.ByteBuffer;" + crlf;
		r += "public class " + cN + " implements CPUProvement.DynamicProblem {"
				+ crlf;

		r += "private MessageDigest md;" + crlf;

		r += "	public strictfp byte[] solve(MessageDigest m, byte[] problemSrc, long argL, double argD) {"
				+ crlf;

		r += "		this.md = m;" + crlf;

		//対象配列の設定 TODO:メンバー変数にしたいがそうすると実行するたびに結果が変わる謎バグが出る
		r += "long[] lVar = {" + crlf;
		for (int i = 0; i < lVar.length; i++) {
			r += ByteBuffer.wrap(h).getLong(i) + "L,";
		}
		r = r.substring(0, r.length() - 1);
		r += "};" + crlf;

		r += "double[] dVar = {" + crlf;
		for (int i = 0; i < dVar.length; i++) {
			Double d = ByteBuffer.wrap(h).getDouble(i);
			if (Double.isFinite(d)) {
				r += d + ",";
			} else {
				r += dInit + ",";
			}
		}
		r = r.substring(0, r.length() - 1);
		r += "};" + crlf;

		r += "char[] cVar = { 't', '9', '2', 'Y', 'u', 'w', 'W', 'a', 'h', '1', '2',"
				+ "	'Z', 'A', 'F', 'D', 'Z', 's', 'l', '2', 'd', 'u', 'l', 'W', 'Z',"
				+ "	'm', 'l', 'G', 'b', 'g', 'k', 'n', 'Y' };" + crlf;

		r += "		for(int i=0; i<lVar.length; i++)" + crlf;
		r += "			lVar[i] += argL; " + crlf;
		r += "		for(int i=0; i<dVar.length; i++)" + crlf;
		r += "			dVar[i] += argD; " + crlf;
		r += "		for(int i=1; i<cVar.length; i++)" + crlf;
		r += "			cVar[i] += cVar[i] + cVar[i-1];" + crlf;

		r += "		for(int j=0; j<" + loop + ";j++){" + crlf;
		for (int i = 0; i < internalFuncCount; i++) {
			r += "			method" + i + "(0, lVar, dVar); " + crlf;
		}
		r += "		}" + crlf;

		r += "		for(int i=0;i<" + dVar.length + ";i++){ " + crlf;
		r += "			md.update(ByteBuffer.allocate(8).putDouble(dVar[i]).array()); "
				+ crlf;
		r += "		}" + crlf;
		r += "		for(int i=0;i<" + lVar.length + ";i++){ " + crlf;
		r += "			md.update(ByteBuffer.allocate(8).putLong(lVar[i]).array()); "
				+ crlf;
		r += "		}" + crlf;
		//ここでproblemSrc,argL,argDに依存させる事で引数による出力値の変化具合がどの問題でも同じになる。
		//簡単な問題関数でなければ作り直す、という行為を無くす事ができる。
		//今の問題関数と新しい問題関数のどちらがより正しい引数を見つけやすいかが評価不能になる。
		r += "		md.update(problemSrc); " + crlf;
		r += "		md.update(ByteBuffer.allocate(8).putLong(argL).array()); "
				+ crlf;
		r += "		md.update(ByteBuffer.allocate(8).putDouble(argD).array()); "
				+ crlf;
		r += "		return md.digest(); " + crlf;
		r += "	}" + crlf;

		for (int i = 0; i < internalFuncCount; i++) {
			r += "		public void method" + i
					+ "(int nest, long[] lVar, double[] dVar) { " + crlf;
			r += "			" + generateMethodContent(hashes[i]) + crlf;
			r += "			commonRecursive(nest, lVar, dVar); " + crlf;
			r += "		}" + crlf;
		}

		//再起呼び出しの共通関数
		r += "	public void commonRecursive(int nest, long[] lVar, double[] dVar){ "
				+ crlf;
		r += "		if(nest > " + recursiveMax + "){ return; } " + crlf;
		r += "		int j=0; " + crlf;
		r += "		for(int i=0;i<lVar.length;i++) { " + crlf;
		r += "			if(lVar[i] > 0) { " + crlf;
		r += "				j = (int)(lVar[i] % " + internalFuncCount + "L);"
				+ crlf;
		r += "			}" + crlf;
		r += "		}" + crlf;

		r += "		for(int k=0; k<" + recursiveLoop + ";k++){" + crlf;
		r += "			switch(j) {" + crlf;
		for (int methodCount = 0; methodCount < internalFuncCount; methodCount++) {
			r += "				case " + methodCount + ":" + crlf;
			r += "					method" + methodCount
					+ "(nest+1, lVar, dVar);" + crlf;
			r += "					break;" + crlf;
		}
		r += "				default:" + crlf;
		r += "			}" + crlf;
		r += "		}" + crlf;

		r += "	}" + crlf;

		r += "}" + crlf;

		return r;
	}

	/**
	 * 一項演算
	 */
	public String ope1(byte[] h, String indent) {
		String r = indent + "for(int i=0;i<" + lVar.length + ";i++){" + crlf;

		r += indent + "	if(" + lVarStr + "[i] == 0){" + crlf;
		r += indent + "		" + lVarStr + "[i] = " + lInit + ";" + crlf;
		r += indent + "	}" + crlf;

		r += indent + "}" + crlf;

		for (int i = 0; i < h.length && i < ope1max; i++) {
			String v1 = lVarStr + "[" + (Byte.toUnsignedInt(h[i]) % lVar.length)
					+ "]";
			switch (Byte.toUnsignedInt(h[i]) % 4) {
			case 0:
				r += indent + v1 + "=~" + v1 + ";" + crlf;
				break;
			case 1:
				r += indent + v1 + "=" + v1 + ">>2;" + crlf;
				break;
			case 2:
				r += indent + v1 + "=" + v1 + ">>>2;" + crlf;
				break;
			case 3:
				r += indent + v1 + "=" + v1 + "<<4;" + crlf;
				break;
			default:
			}

		}
		return r;
	}

	/**
	 * 二項演算
	 */
	public String ope2(byte[] h, String indent) {
		String r = "";
		r += ope2internal(h, lVar, lOpe2, lInit, lVarStr, indent);
		r += ope2internal(h, dVar, dOpe2, dInit, dVarStr, indent);
		return r;
	}

	public String ope2internal(byte[] h, String[] vars, String[] opes,
			String init, String varArrayName, String indent) {
		String r = "";
		for (int i = 0; i < h.length && i < ope2max; i++) {
			String v1 = varArrayName + "["
					+ (Byte.toUnsignedInt(h[i]) % vars.length) + "]";
			String v2 = varArrayName + "["
					+ (Byte.toUnsignedInt(h[h.length - i - 1]) % vars.length)
					+ "]";
			String op = opes[Byte.toUnsignedInt(h[i]) % opes.length];
			r += indent + "if(" + v1 + "==0 || !Double.isFinite(" + v1 + ")){"
					+ v1 + "=" + init + ";}" + crlf;
			r += indent + "if(" + v2 + "==0 || !Double.isFinite(" + v2 + ")){"
					+ v2 + "=" + init + ";}" + crlf;
			r += indent + v1 + "=" + v1 + op + v2 + ";" + crlf;
		}
		return r;
	}

	public String lineOpe(byte[] h, String[] vars, String[] opes, String init,
			String varArrayName) {
		String[] op = sort(opes, h);
		String[] va = sort(vars, h);
		String r = varArrayName + "[0]";
		String bracket = "";
		for (int i = 0; i < va.length & i < op.length; i++) {
			String oTmp = op[i];
			String vTmp = varArrayName + "[" + i + "]";
			if (oTmp.equals("/") || oTmp.equals("%")) {
				vTmp = "(" + vTmp + "==0 ? " + init + " : " + vTmp + ")";
			}

			bracket += "(";
			r += oTmp + vTmp + ")";
		}
		r = bracket + r;
		return r;
	}

	public String generateMethodContent(byte[] hSrc) {
		String r = "";
		String indent = "		";
		String indent2 = "			";

		md.reset();

		md.update(hSrc);
		md.update((byte) 97);//generateMethodContent専用のハッシュ値を作るため
		byte[] h1 = md.digest();//resetされる

		md.update(hSrc);
		md.update((byte) 103);
		byte[] h2 = md.digest();
		for (int i = 0; i < internalFuncSize; i++) {
			h1 = md.digest(h1);
			h2 = md.digest(h2);

			r += indent + "if((" + lineOpe(h1, lVar, lOpe2, lInit, lVarStr)
					+ ">" + lineOpe(h2, lVar, lOpe2, lInit, lVarStr) + ")=="
					+ crlf + "(" + lineOpe(h1, dVar, dOpe2, dInit, dVarStr)
					+ ">" + lineOpe(h2, dVar, dOpe2, dInit, dVarStr) + crlf
					+ ")){" + crlf;

			r += ope1(h1, indent2);
			r += ope2(h1, indent2);

			r += indent + "}else{" + crlf;

			r += ope1(h2, indent2);
			r += ope2(h2, indent2);

			r += indent + "}" + crlf;
		}
		return r;
	}

	/**
	 * ハッシュに依存して演算子の順序を決定する。h.length > src.length
	 */
	public String[] sort(String[] src, byte[] h) {
		List<String> l = new LinkedList<>(Arrays.asList(src));
		String[] r = new String[src.length];
		for (int i = 0; l.size() > 0; i++) {
			int unsigned = Byte.toUnsignedInt(h[i]);
			int index = unsigned % l.size();
			r[i] = l.get(index);
			l.remove(index);
		}

		return r;
	}

	public boolean verify(Solve answer) {
		long start = System.currentTimeMillis();
		reset();
		verifyMode = true;
		this.answer = answer;
		boolean r = compute() != null;
		long end = System.currentTimeMillis();
		Glb.debug("検証 : " + (end - start) + "ms");
		return r;
	}

	/**
	 * @return 計算結果。getResult()と同じ。
	 */
	public Result solve() {
		long start = System.currentTimeMillis();
		reset();
		verifyMode = false;
		Result r = compute();
		long end = System.currentTimeMillis();
		Glb.debug("回答 : " + (end - start) + "ms");
		return r;
	}

	/**
	 * @return verifyModeでnullなら否定
	 */
	private Result compute() {
		if (!solveSetup())
			return null;
		return new Result(solved, p);
	}

	private boolean solveSetup() {
		boolean r = false;
		if (verifyMode) {
			Solve verified = execute(dp, answer.getArgL(), answer.getArgD());
			if (verified != null) {
				solved = verified;
				r = true;
			}
		} else {
			for (int i = 0; i < 1000 * 1000; i++) {
				long argLCandidate = ThreadLocalRandom.current()
						.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
				double argDCandidate = ThreadLocalRandom.current()
						.nextDouble(Double.MIN_VALUE, Double.MAX_VALUE);

				//TODO:もし解が密集している区間があるならその区間を禁止する処理を入れる
				//検証でも禁止処理をすれば利用されない

				Solve candidate = execute(dp, argLCandidate, argDCandidate);
				if (candidate == null)
					continue;
				r = true;
				solved = candidate;
				Glb.debug("引数探索に要した回数:" + i);
				break;
			}
		}
		return r;
	}

	private boolean isValid(byte[] candidateHash) {
		//ループ回数を引き上げれば正しい出力の範囲を狭めれる
		for (int i = 0; i < outputRestrictCount; i++) {
			if (candidateHash[i] != outputRestrictValue) {
				return false;
			}
		}
		return true;
	}

	private Solve execute(DynamicProblem dp, long argLCandidate,
			double argDCandidate) {
		// DynamicProblem#solve()はこのCPUProvementクラスを読み込んだローダーによって依存関係が解決される。
		// Problem<Number>.javaを読み込んだloaderではない。
		md.reset();
		byte[] candidateOutput = dp.solve(md, p.getHash(), argLCandidate,
				argDCandidate);
		md.reset();
		//		byte[] candidateHash = md.digest(candidateOutput);	問題関数の出力部でハッシュ関数を通しているので不要
		if (isValid(candidateOutput)) {
			return new Solve(candidateOutput, argLCandidate, argDCandidate);
		} else {
			return null;
		}
	}

	public static interface DynamicProblem {
		public byte[] solve(MessageDigest md, byte[] problemSrc, long argL,
				double argD);
	}

	public String getCode() {
		return code;
	}

	/**
	 * 並列処理で出来るだけたくさん解く。
	 * parallelNumberが変化することで問題が変化する。
	 * @param answers	回答を格納するリスト
	 * @param endTime	この時間までできるだけ多くの回答を作成する
	 * @param src		問題作成情報
	 */
	public static void parallelSolve(ResultList answers, final long endTime,
			final ProblemSrc src) {
		MultiThreadTask t = new MultiThreadTask() {
			@Override
			public void call(int taskId) throws Exception {
				CPUProvement p = new CPUProvement(new ProblemSrc(src, taskId));
				Result r = p.solve();
				answers.register(r.getProblemSrc().getParallelNumber(),
						r.getSolve());
			}
		};

		Glb.getUtil().parallelTask(endTime, P2PEdge.getScoreMax(), t);
	}
}
