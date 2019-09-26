package bei7473p5254d69jcuat.tenyu.sample;

import static org.junit.Assert.*;

import java.security.*;
import java.util.*;

import org.junit.*;

import bei7473p5254d69jcuat.tenyu.sample.CPUProvementSample2018.*;

public class CPUProvementTestSample2018 {

	@Test
	public void test() throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(2048);
		KeyPair keyPair = keyPairGen.genKeyPair();
		PublicKey respondentPub = keyPair.getPublic();

		byte[] respondentPubB = respondentPub.getEncoded();

		//問題作成	これらの入力から問題関数が作成される
		CPUProvementSample2018 problem = new CPUProvementSample2018("aaaa",
				2017, 12, 17, 0, 1, respondentPubB);
		//回答計算をして結果が返る
		ResultSample result = problem.solve();
		//引数探索の結果を取得
		SolveSample solve = result.getSolve();
		System.out.println("output:" + Arrays.toString(solve.getOutput()));
		System.out.println("argD:" + solve.getArgD());
		System.out.println("argL:" + solve.getArgL());
		//検証	問題と結果があれば検証できる
		assertEquals(true, problem.verify(solve));
	}
}
