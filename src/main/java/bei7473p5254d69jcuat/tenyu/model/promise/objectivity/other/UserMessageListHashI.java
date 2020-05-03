package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.other;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;

public interface UserMessageListHashI extends AdministratedObjectI{
	long getHistoryIndex();
	void setHistoryIndex(long historyIndex);
}
