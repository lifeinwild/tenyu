package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.*;
import bei7473p5254d69jcuat.tenyu.model.promise.reference.*;
import bei7473p5254d69jcuat.tenyu.model.release1.reference.*;

public interface IndividualityObjectI extends AdministratedObjectI, HasTag {
	public static final int tagIdsMax = 40;

	String getExplanation();

	//void setExplanation(String explanation);

	String getName();

	/**
	 * @return	名前の最大長
	 */
	default int getNameMax() {
		return getNameMaxStatic();
	}

	static int getNameMaxStatic() {
		return 50;
	}

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

	TenyuReferenceArtifactByVersionMajor getCss();

	TenyuReferenceModelI<? extends IndividualityObjectI> getReference();

}
