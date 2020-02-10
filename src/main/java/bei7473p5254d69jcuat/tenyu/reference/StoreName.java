package bei7473p5254d69jcuat.tenyu.reference;

import bei7473p5254d69jcuat.tenyu.db.store.*;
import jetbrains.exodus.env.*;

/**
 * 参照されうるストアの名前。
 * H木ストアなどサブストアは含まれない。
 *
 * 通信系メッセージクラスで、あるいは永続化されるクラスのメンバー変数などで
 * ストア名を指定する場合があり、文字列で指定するよりenumのほうが
 * シリアライザで圧縮できるのでサイズを小さくできるということで作成した。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface StoreName {
	ObjectStore<?, ?> getStore(Transaction txn);

	String getModelName();

	String toString();

}
