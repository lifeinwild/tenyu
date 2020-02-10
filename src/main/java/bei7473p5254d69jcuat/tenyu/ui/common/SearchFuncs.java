package bei7473p5254d69jcuat.tenyu.ui.common;

import java.util.*;
import java.util.function.*;

import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import glb.*;

/**
 * 検索系GUIの抽象化のために作成
 * 具体的なところでこれらラムダ式を定義して抽象的なGUI構築メソッドに渡す
 *
 * @author exceptiontenyu@gmail.com
 *
 * @param <T1>
 * @param <T2>
 * @param <S>
 */
public class SearchFuncs<T1 extends IdObjectDBI, T2 extends T1> {
	/**
	 * 検索結果のクリア処理など
	 */
	private Runnable clearFunc;
	/**
	 * モデルが1件該当した場合の処理
	 */
	private Consumer<T2> singleFunc;
	/**
	 * モデルが複数該当した場合の処理
	 */
	private Consumer<List<T2>> multiFunc;

	public SearchFuncs(Runnable clearFunc, Consumer<T2> singleFunc,
			Consumer<List<T2>> multiFunc) {

		this.clearFunc = clearFunc;
		this.singleFunc = singleFunc;
		this.multiFunc = multiFunc;
	}

	public Runnable getClearFunc() {
		return clearFunc;
	}

	public void setClearFunc(Runnable clearFunc) {
		this.clearFunc = clearFunc;
	}

	public Consumer<T2> getSingleFunc() {
		return singleFunc;
	}

	public void setSingleFunc(Consumer<T2> singleFunc) {
		this.singleFunc = singleFunc;
	}

	public Consumer<List<T2>> getMultiFunc() {
		return multiFunc;
	}

	public void setMultiFunc(Consumer<List<T2>> multiFunc) {
		this.multiFunc = multiFunc;
	}

}