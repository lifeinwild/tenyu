package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality;

import bei7473p5254d69jcuat.tenyu.release1.db.*;

public interface NaturalityDBI extends ObjectivityObjectDBI {
	String getExplanation();

	void setExplanation(String explanation);

	String getName();

	void setName(String name);
}
