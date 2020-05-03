package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.individuality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.tenyupedia.*;
import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.reference.*;

public interface IndividualityObjectI extends AdministratedObjectI {
	public static final int tagIdsMax = 40;
	public static final int tagMax = 200;

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

	/**
	 * @return	このオブジェクトが主にどの言語を使用しているか
	 */
	Locale getLocale();

	/**
	 * @return	Language display name
	 */
	default String getLocaleStr() {
		return getLocaleStrStatic(getLocale());
	}

	static String getLocaleStrStatic(Locale l) {
		return l.getDisplayLanguage(Locale.ENGLISH);
	}

	/**
	 * @return	このオブジェクトに関連するタグ
	 */
	List<Long> getTagIds();

	boolean addTag(Long tagId);

	/**
	 * @param tag
	 * @return	追加されたか
	 */
	boolean addTag(String tag);

	boolean addTag(Tag t);

	TenyutalkReferenceFlexible<? extends CreativeObjectI> getCss();

}
