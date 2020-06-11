package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import glb.*;

/**
 * 客観に登録される成果物メタデータ
 * ただし成果物の一部メタデータ
 *
 * 関連：{@link TenyutalkArtifactI}
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyuArtifactI extends IndividualityObjectI, HasSocialityI {
	/**
	 * アクセスされたら勝手に広まるので人気のあるファイルは1でいい。
	 *
	 * 高いほどハッシュ署名や読み取り署名の上限が上がり、ミラーが増える。
	 * @return	積極的公開度
	 */
	int getActivePublication();

	/**
	 * @return	このアーティファクトを管理しているリポジトリ
	 */
	default TenyuRepositoryI getTenyuRepository() {
		return Glb.getObje()
				.getTenyuRepository(trs -> trs.get(getTenyuRepositoryId()));
	}

	/**
	 * @return	このアーティファクトを管理しているリポジトリのID
	 */
	Long getTenyuRepositoryId();

	/**
	 * @return	この成果物メタデータへの署名名目
	 */
	default String getSignNominal() {
		return Glb.getConf().getKeys()
				.getSignNominal(TenyuArtifact.class.getSimpleName());
	}

}
