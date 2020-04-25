package bei7473p5254d69jcuat.tenyu.sample;

import java.nio.*;
import java.nio.charset.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;

import org.codehaus.commons.compiler.*;
import org.codehaus.janino.*;

/**
 * CPU証明｡あるいはﾌﾟﾛｾｯｻ証明のCPU型｡
 *
 * プロセッサ証明に関する大抵のコードはここに書かれている。
 * マルチスレッドで処理して演算量を稼ぐインターフェースがある。
 * プロセッサ証明に関してはReadme参照。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class CPUProvementSample2018 {
	public static interface DynamicProblem {
		public byte[] solve(MessageDigest md, long argL, double argD);
	}

	public static interface MultiThreadTask {
		public abstract void call(int taskId) throws Exception;
	}

	/**
	 * 問題作成情報
	 */
	public static class ProblemSrcSample {
		/**
		 * 近傍から届いたﾗﾝﾀﾞﾑ値を連結したもの
		 */
		private String rndStr;
		/**
		 * 問題を多数作成するための任意の情報としての連番
		 */
		private int parallelNumber;
		private int year;
		private int month;
		private int day;
		private int hour;
		/**
		 * 回答者公開鍵
		 */
		private byte[] pub;

		public ProblemSrcSample() {
		}

		/**
		 * parallelNumberだけ変えて既存のｲﾝｽﾀﾝｽから複製する
		 */
		public ProblemSrcSample(ProblemSrcSample src, int parallelNumber) {
			this.rndStr = src.getRndStr();
			this.year = src.getYear();
			this.month = src.getMonth();
			this.day = src.getDay();
			this.hour = src.getHour();
			this.pub = src.getPub();
			this.parallelNumber = parallelNumber;
		}

		public ProblemSrcSample(String rndStr) {
			this.rndStr = rndStr;
			parallelNumber = 0;
			Calendar c = Calendar.getInstance(Locale.JAPAN);
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DATE);
			hour = c.get(Calendar.HOUR);
			byte[] rnd = new byte[512];
			Random r = new Random();
			r.nextBytes(rnd);
			pub = rnd;//本当は自分の公開鍵を設定するがその部分のｼｽﾃﾑと分離したので仮の値を設定
		}

		public ProblemSrcSample(String rndStr, int year, int month, int day,
				int hour, int parallelNumber, byte[] pub) {
			this.rndStr = rndStr;
			this.year = year;
			this.month = month;
			this.day = day;
			this.hour = hour;
			this.pub = pub;
			this.parallelNumber = parallelNumber;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ProblemSrcSample))
				return false;
			ProblemSrcSample s = (ProblemSrcSample) o;

			return rndStr.equals(s.getRndStr())
					&& parallelNumber == s.getParallelNumber()
					&& year == s.getYear() && month == s.getMonth()
					&& day == s.getDay() && hour == s.getHour()
					&& Arrays.equals(pub, s.getPub());
		}

		public String getClassName() {
			return "Problem" + parallelNumber;
		}

		public int getDay() {
			return day;
		}

		public byte[] getHash(MessageDigest md) {
			md.reset();
			md.update(rndStr.getBytes(Charset.forName("UTF-8")));
			md.update(toBytes(year));
			md.update(toBytes(month));
			md.update(toBytes(day));
			md.update(toBytes(hour));
			md.update(toBytes(parallelNumber));
			md.update(pub);
			return md.digest();
		}

		public int getHour() {
			return hour;
		}

		public int getMonth() {
			return month;
		}

		public int getParallelNumber() {
			return parallelNumber;
		}

		public byte[] getPub() {
			return pub;
		}

		public String getRndStr() {
			return rndStr;
		}

		public int getYear() {
			return year;
		}

		public boolean isNotNullDeep() {
			if (rndStr == null || year == 0 || month == 0 || day == 0)
				return false;
			return true;
		}

		public final byte[] toBytes(int data) {
			byte[] b = new byte[4];

			for (int i = 0; i < 4; i++) {
				b[3 - i] = (byte) (data >>> i * 8);
			}

			return b;
		}

	}

	/**
	 * 回答｡回答者から出題者に送信される｡
	 */
	public static class ResultSample {
		public static int getMaxSize() {
			return 1000;
		}

		private SolveSample solve;

		private ProblemSrcSample problemSrc;

		public ResultSample() {
		}

		public ResultSample(SolveSample solve, ProblemSrcSample problemSrc) {
			this.solve = solve;
			this.problemSrc = problemSrc;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ResultSample))
				return false;
			ResultSample r = (ResultSample) o;
			return solve.equals(r.getSolve())
					&& problemSrc.equals(r.getProblemSrc());
		}

		public ProblemSrcSample getProblemSrc() {
			return problemSrc;
		}

		public SolveSample getSolve() {
			return solve;
		}

		public boolean isNotNullDeep() {
			if (solve == null || problemSrc == null)
				return false;
			return solve.isNotNullDeep() && problemSrc.isNotNullDeep();
		}
	}

	/**
	 * 問題関数の出力値と探索された引数
	 */
	public static class SolveSample {
		private byte[] output;
		private long argL;
		private double argD;

		public SolveSample() {
		}

		public SolveSample(byte[] output, long argL, double argD) {
			this.output = output;
			this.argL = argL;
			this.argD = argD;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof SolveSample))
				return false;
			SolveSample sol = (SolveSample) o;
			return Arrays.equals(output, sol.getOutput())
					&& argL == sol.getArgL() && argD == sol.getArgD();
		}

		public double getArgD() {
			return argD;
		}

		public long getArgL() {
			return argL;
		}

		public byte[] getOutput() {
			return output;
		}

		public boolean isNotNullDeep() {
			if (output == null)
				return false;
			return true;
		}
	}

	public static Logger logger = LogManager.getLogManager().getLogger("");
	private static final int scoreMax = 100;

	public static int getThreadNum() {
		int threadNum = Runtime.getRuntime().availableProcessors() - 1;
		if (threadNum < 1)
			threadNum = 1;//最低1ｽﾚｯﾄﾞ
		return threadNum;
	}

	/**
	 * 全ｺｱ-1個のｺｱを使って並列処理で出来るだけたくさん解く｡
	 * parallelNumberが変化することで問題が変化する｡
	 */
	public static List<ResultSample> parallelSolve(final long endTime,
			final ProblemSrcSample src) {
		//問題と処理結果を格納するﾘｽﾄ
		List<ResultSample> solveds = Collections
				.synchronizedList(new ArrayList<>());

		MultiThreadTask t = new MultiThreadTask() {
			@Override
			public void call(int taskId) throws Exception {
				CPUProvementSample2018 p = new CPUProvementSample2018(
						new ProblemSrcSample(src, taskId));
				solveds.add(p.solve());
			}
		};

		parallelTask(endTime, scoreMax, t);
		return solveds;
	}

	/**
	 * ﾀｽｸを並列処理する｡
	 * この方法はtという1ｲﾝｽﾀﾝｽがﾏﾙﾁｽﾚｯﾄﾞで実行されるので
	 * 内部状態など注意が必要｡
	 * @param endTime		この時間まで出来るだけたくさん処理する
	 * @param ThreadSolveMax	1ｽﾚｯﾄﾞあたりの最大t.call()回数
	 * @param t				外部から渡される処理
	 */
	public static void parallelTask(long endTime, int ThreadSolveMax,
			MultiThreadTask t) {
		ExecutorService threadpool = null;
		try {
			//ｺｱ数-1個のｽﾚｯﾄﾞで並列に処理する
			int threadNum = getThreadNum();
			AtomicInteger ai = new AtomicInteger(0);

			//処理内容の設定
			Collection<Callable<Void>> jobs = new ArrayList<Callable<Void>>();
			for (int i = 0; i < threadNum; i++) {
				jobs.add(new Callable<Void>() {
					@Override
					public Void call() throws Exception {
						//時間内に出来るだけたくさん解く
						try {
							int solvedThisThread = 0;
							long solveStart = System.currentTimeMillis();
							long expect = 1;
							while (expect > 0) {
								int taskId = ai.getAndIncrement();
								t.call(taskId);
								solvedThisThread++;

								if (solvedThisThread > ThreadSolveMax)
									break;

								//経過時間
								long elapsed = System.currentTimeMillis()
										- solveStart;
								//1回あたりの時間
								long perSolve = elapsed / solvedThisThread;
								//残り時間
								long solveRemaining = endTime
										- System.currentTimeMillis();

								//残り時間で期待される回数
								if (perSolve > 0)
									expect = solveRemaining / perSolve;
							}
						} catch (Exception e) {
						}
						return null;
					}
				});
			}

			//並列処理開始
			threadpool = Executors.newFixedThreadPool(threadNum);
			threadpool.invokeAll(jobs);
		} catch (Exception e) {
		} finally {
			if (threadpool != null)
				threadpool.shutdownNow();
		}
	}

	private MessageDigest md;

	private SimpleCompiler compiler;
	private final String packageStr = "tenyu.sample.dynamicprogram";

	private final String[] lOpe2 = { "+", "-", "/", "*", "|", "&", "^", "%" },
			dOpe2 = { "+", "-", "/", "*" };

	private volatile String[] lVar = { "l1", "l2", "l3", "l4", "l5", "l6", "l7",
			"l8", "l9", "l10" },
			dVar = { "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9",
					"d10" };

	private String loop;

	private String recursiveLoop = "2";
	private final String lInit = "123456789L", dInit = "3.141592D";

	private final String lVarStr = "lVar", dVarStr = "dVar";
	private final int ope1max = 2, ope2max = 2;
	/**
	 * 引数探索における正しい出力値は頭に0がいくつか続いている事を条件にするが
	 * その0の数｡1なら検証者は回答者の2^8分の1の負荷で検証できる｡2なら2^16分の1
	 */
	private final int outputZeroCount = 1;

	/**
	 * 問題関数の内部関数のｻｲｽﾞを調節できる｡
	 * Javaは1ﾒｿｯﾄﾞの最大ｻｲｽﾞが64kbという制限がある｡
	 */
	private final int internalFuncSize = 2;

	/**
	 * 問題関数の内部関数の再起呼び出しのﾈｽﾄの深さ
	 */
	private final int recursiveMax = 2;

	/**
	 * 問題関数の内部関数の数
	 */
	private final int internalFuncCount = 2;

	private final String crlf = System.getProperty("line.separator");

	private String className;

	private String code;

	private ProblemSrcSample p;

	private SolveSample solved;

	private SolveSample answer;

	private boolean verifyMode = false;

	private DynamicProblem dp;

	public CPUProvementSample2018(ProblemSrcSample p) throws Exception {
		this.p = p;
		setup();
	}

	public CPUProvementSample2018(String rndStr, int year, int month, int day,
			int hour, int parallelNumber, byte[] pub) throws Exception {
		ProblemSrcSample p = new ProblemSrcSample(rndStr, year, month, day,
				hour, parallelNumber, pub);
		this.p = p;
		setup();
	}

	private void appendJavaStr(StringBuilder sb, String src) {
		for (char c : src.toCharArray()) {
			if (Character.isJavaIdentifierPart(c)) {
				sb.append(c);
			}
		}
	}

	/**
	 * @return verifyModeでnullなら否定
	 */
	private ResultSample compute() {
		if (!solveSetup())
			return null;
		return new ResultSample(solved, p);
	}

	private SolveSample execute(DynamicProblem dp, long argLCandidate,
			double argDCandidate) {
		// DynamicProblem#solve()はこのCPUProvementｸﾗｽを読み込んだﾛｰﾀﾞｰによって依存関係が解決される｡
		// Problem<Number>.javaを読み込んだloaderではない｡
		md.reset();
		byte[] candidateOutput = dp.solve(md, argLCandidate, argDCandidate);
		md.reset();
		byte[] candidateHash = md.digest(candidateOutput);
		if (isValid(candidateHash)) {
			return new SolveSample(candidateOutput, argLCandidate,
					argDCandidate);
		} else {
			return null;
		}
	}

	public String generateMethodContent(byte[] hSrc) {
		String r = "";
		String indent = "		";
		String indent2 = "			";

		md.reset();

		md.update(hSrc);
		md.update((byte) 97);//generateMethodContent専用のﾊｯｼｭ値を作るため
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
	 * 動的な問題関数の作成｡
	 * 対象配列があり､そのﾊｯｼｭ値が出力になるので､
	 * 普遍的に､つまりあらゆる問題作成情報で､
	 * 解が特定の区間に密集するという事は無い｡
	 * 特定の問題作成情報に限定すればあるかもしれない｡
	 * 各演算の性質によってある引数候補を調べると別の引数候補を
	 * 調べなくていいということは普遍的にあるかもしれない｡
	 * それは出力が同じになる場合である｡
	 * しかし問題関数の作成方法自体随時変更していくつもりなので､
	 * 厳密に問題関数の性質を考えるｺｽﾄをかけるべきと思わない｡
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
		r += "import " + CPUProvementSample2018.class.getPackage().getName()
				+ ".*;" + crlf;
		r += "import java.security.MessageDigest;" + crlf;
		r += "import java.nio.ByteBuffer;" + crlf;
		r += "public class " + cN
				+ " implements CPUProvementSample2018.DynamicProblem {" + crlf;

		r += "private MessageDigest md;" + crlf;

		r += "	public byte[] solve(MessageDigest m, long argL, double argD) {"
				+ crlf;

		r += "		this.md = m;" + crlf;

		//対象配列の設定 TODO:ﾒﾝﾊﾞｰ変数にしたいがそうすると実行するたびに結果が変わる謎ﾊﾞｸﾞが出る
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
		r += "			lVar[i] += argL + cVar[i]; " + crlf;
		r += "		for(int i=0; i<dVar.length; i++)" + crlf;
		r += "			dVar[i] += argD; " + crlf;

		r += "		for(int j=0; j<" + loop + ";j++){" + crlf;
		for (int i = 0; i < internalFuncCount; i++) {
			r += "			method" + i + "(0, lVar, dVar);" + crlf;
		}
		r += "		}" + crlf;

		r += "		for(int i=0;i<" + dVar.length + ";i++){" + crlf;
		r += "			byte[] db = new byte[8];" + crlf;
		r += "			ByteBuffer.wrap(db).putDouble(dVar[i]);" + crlf;
		r += "			md.update(db);" + crlf;
		r += "		}" + crlf;
		r += "		for(int i=0;i<" + lVar.length + ";i++){" + crlf;
		r += "			byte[] lb = new byte[8];" + crlf;
		r += "			ByteBuffer.wrap(lb).putDouble(lVar[i]);" + crlf;
		r += "			md.update(lb);" + crlf;
		r += "		}" + crlf;
		r += "		return md.digest();" + crlf;
		r += "	}" + crlf;

		for (int i = 0; i < internalFuncCount; i++) {
			r += "		public void method" + i
					+ "(int nest, long[] lVar, double[] dVar) {" + crlf;
			r += "			" + generateMethodContent(hashes[i]) + crlf;
			r += "			commonRecursive(nest, lVar, dVar);" + crlf;
			r += "		}" + crlf;
		}

		//再起呼び出しの共通関数
		r += "	public void commonRecursive(int nest, long[] lVar, double[] dVar){"
				+ crlf;
		r += "		if(nest > " + recursiveMax + "){ return; }" + crlf;
		r += "		int j=0;" + crlf;
		r += "		for(int i=0;i<lVar.length;i++) {" + crlf;
		r += "			if(lVar[i] > 0) {" + crlf;
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

	public String getCode() {
		return code;
	}

	private boolean isValid(byte[] candidateHash) {
		//ﾙｰﾌﾟ回数を引き上げれば正しい出力の範囲を狭めれる
		for (int i = 0; i < outputZeroCount; i++) {
			if (candidateHash[i] != 0) {
				return false;
			}
		}
		return true;
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

	/**
	 * 計算のためのﾘｾｯﾄ｡問題作成はｺﾝｽﾄﾗｸﾀだけなのでﾘｾｯﾄされない｡
	 */
	private void reset() {
		verifyMode = false;
		solved = null;
		answer = null;
		md.reset();
	}

	private void setup() throws Exception {
		long start = System.currentTimeMillis();
		loop = "1";
		//ﾊｯｼｭ作成ｵﾌﾞｼﾞｪｸﾄ
		md = MessageDigest.getInstance("SHA-512");

		//ｺﾝﾊﾟｲﾗ関係
		//Janinoによるｵﾝﾒﾓﾘｺﾝﾊﾟｲﾙ｡
		//Java9からtools.jarが削除されたのでJDKから変更
		compiler = new SimpleCompiler();
		compiler.setCompileErrorHandler(new ErrorHandler() {
			@Override
			public void handleError(String message, Location optionalLocation)
					throws CompileException {
				//CPUProvementSample2018.logger.info(
				//		message + "Location:" + optionalLocation.toString());
			}
		});

		//動的作成されるｸﾗｽの名前の末尾
		StringBuilder suffix = new StringBuilder();
		//多数の近傍ﾉｰﾄﾞが送信してきた回答を並列に検証するためにpubStrをつける
		//つけないとﾌｧｲﾙ名が被って誤作動する
		byte[] b = md.digest(p.getPub());
		String classNameSrc = Base64.getEncoder().encodeToString(b);
		appendJavaStr(suffix, "_" + classNameSrc);
		//他のｽﾚｯﾄﾞとｸﾗｽ名衝突を回避するため並列番号を付加する
		className = "Problem" + p.getParallelNumber() + suffix;

		//ﾊｯｼｭから動的ｺｰﾄﾞを作成
		code = generateProblem(p.getHash(md), className);

		//ｺﾝﾊﾟｲﾗ用ﾀｽｸ作成
		compiler.cook(code);

		//動的ﾌﾟﾛｸﾞﾗﾑのﾛｰﾄﾞ
		Class<?> heaped = compiler.getClassLoader()
				.loadClass(packageStr + "." + className);
		if (heaped == null) {
			CPUProvementSample2018.logger.info("failure");
			return;
		}
		Object o = heaped.getDeclaredConstructor().newInstance();
		if (o == null || !(o instanceof DynamicProblem)) {
			CPUProvementSample2018.logger.info("failure");
			return;
		}
		dp = (DynamicProblem) o;

		long end = System.currentTimeMillis();
		//CPUProvementSample2018.logger.info("作成: " + (end - start) + "ms");
	}

	/**
	 * @return 計算結果｡getResult()と同じ｡
	 */
	public ResultSample solve() {
		long start = System.currentTimeMillis();
		reset();
		verifyMode = false;
		ResultSample r = compute();
		long end = System.currentTimeMillis();
		//CPUProvementSample2018.logger.info("回答 : " + (end - start) + "ms");
		return r;
	}

	private boolean solveSetup() {
		boolean r = false;
		if (verifyMode) {
			SolveSample verified = execute(dp, answer.getArgL(),
					answer.getArgD());
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

				SolveSample candidate = execute(dp, argLCandidate,
						argDCandidate);
				if (candidate == null)
					continue;
				r = true;
				solved = candidate;
				//CPUProvementSample2018.logger.info("引数探索に要した回数:" + i);
				break;
			}
		}
		return r;
	}

	/**
	 * ﾊｯｼｭに依存して演算子の順序を決定する｡h.length > src.length
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

	public boolean verify(SolveSample answer) {
		long start = System.currentTimeMillis();
		reset();
		verifyMode = true;
		this.answer = answer;
		boolean r = compute() != null;
		long end = System.currentTimeMillis();
		//CPUProvementSample2018.logger.info("検証 : " + (end - start) + "ms");
		return r;
	}
}