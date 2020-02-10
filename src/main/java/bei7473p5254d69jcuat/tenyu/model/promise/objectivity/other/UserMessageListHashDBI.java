package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.other;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;

public interface UserMessageListHashDBI extends AdministratedObjectDBI{
	long getHistoryIndex();
	void setHistoryIndex(long historyIndex);
}
