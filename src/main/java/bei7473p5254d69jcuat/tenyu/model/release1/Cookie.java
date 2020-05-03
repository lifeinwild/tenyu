package bei7473p5254d69jcuat.tenyu.model.release1;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.ui.*;
import glb.util.*;
import jetbrains.exodus.env.*;

/**
 * 任意の{@link ObjectGui}から利用される、ユーザー固有の任意のデータを格納できる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Cookie implements StorableI {
	private Map<String, String> data = new LinkedHashMap<>();

	public Map<String, String> getData() {
		return Collections.unmodifiableMap(data);
	}

	@Override
	public boolean validateAtCreate(ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean validateAtUpdate(ValidationResult r) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean validateReference(ValidationResult r, Transaction txn)
			throws Exception {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

}
