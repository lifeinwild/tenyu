package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.store.*;

public interface IndividualityObjectDBI extends AdministratedObjectDBI {
	/**
	 * タグの最大数
	 */
	int tagSizeMax = 50;
	/**
	 * タグの最大長
	 */
	int tagLenMax = 50;

	/**
	 * もはや人々においてタグは慣れ親しんだものであり説明文の補助として理解できる。
	 *
	 * @return	タグ一覧。Nullable
	 */
	HashSet<String> getTags();

	String getExplanation();

	//void setExplanation(String explanation);

	String getName();

	//void setName(String name);
}
