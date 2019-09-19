package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.other;

import bei7473p5254d69jcuat.tenyu.release1.db.*;

public interface UserMessageListHashDBI extends ObjectivityObjectDBI{
	long getHistoryIndex();
	void setHistoryIndex(long historyIndex);
}
