package bei7473p5254d69jcuat.tenyu.reference;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 参照されうるストアの名前。
 * H木ストアなどサブストアは含まれない。
 *
 * 通信系メッセージクラスで、あるいは永続化されるクラスのメンバー変数などで
 * ストア名を指定する場合があり、文字列で指定するよりenumのほうが
 * シリアライザで圧縮できるのでサイズを小さくできるということで作成した。
 *
 * enumという時点でkryoがordinalで書き込むので
 * {@link StorableI}のための実装は必要ない
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface StoreNameEnum extends StoreName {

	@Override
	default boolean validateAtCreate(ValidationResult r) {
		return true;
	}

	@Override
	default boolean validateAtUpdate(ValidationResult r) {
		return true;
	}

	@Override
	default boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		return true;
	}
}
