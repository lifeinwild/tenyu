package bei7473p5254d69jcuat.tenyu.release1.global;

import java.util.*;

/**
 * 検証メソッドで使用されエラーメッセージ等を記録する。
 *
 * Tenyu全体の検証戦略について。
 *
 * CRUD4種・単体複合2種＝8種の検証機能が各モデル毎に必要
 * 複合検証はメッセージクラスに書かれるので、モデルクラスが実装すべきは4種の単体検証になる。
 *
 * equals,hashcodeはoptionを使わない
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class ValidationResult {
	public static class ValidationResultElement {
		/**
		 * 問題が見つかったデータの名前
		 */
		private Lang name;
		/**
		 * 問題の内容
		 */
		private Lang message;
		/**
		 * モデル側で何らかの補助情報を付加したい場合
		 */
		private String option = null;

		public ValidationResultElement(Lang name, Lang message) {
			this(name, message, null);
		}

		public ValidationResultElement(Lang name, Lang message, String option) {
			this.name = name;
			this.message = message;
			this.option = option;
		}

		public Lang getMessage() {
			return message;
		}

		public Lang getName() {
			return name;
		}

		public String getOption() {
			return option;
		}

		@Override
		public String toString() {
			return "ValidationResultError " + getName() + "   " + getMessage() + "   " + option;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((message == null) ? 0 : message.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ValidationResultElement other = (ValidationResultElement) obj;
			if (message != other.message)
				return false;
			if (name != other.name)
				return false;
			return true;
		}

	}

	/**
	 * エラーが発生した箇所とエラーメッセージ
	 */
	private List<ValidationResultElement> errors = new ArrayList<>();
	/**
	 * CRUD等の基本文脈の種類
	 */
	private CRUDContext ctx;
	/**
	 * User等のクラス名
	 */
	private String modelClassSimpleName;

	public void add(Lang name, Lang message) {
		errors.add(new ValidationResultElement(name, message));
	}

	public void add(Lang name, Lang message, String option) {
		errors.add(new ValidationResultElement(name, message, option));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String ls = System.lineSeparator();
		if (modelClassSimpleName != null)
			sb.append(modelClassSimpleName + ls);
		if (ctx != null)
			sb.append(ctx + ls);
		for (ValidationResultElement e : errors) {
			if (e.getMessage() != null)
				sb.append(e + ls);
		}
		return sb.toString().trim();
	}

	public List<ValidationResultElement> getErrors() {
		return errors;
	}

	public void setErrors(List<ValidationResultElement> errors) {
		this.errors = errors;
	}

	/**
	 * 検証全体が成功した場合true
	 * 1か所でも失敗すればfalse
	 */
	public boolean isNoError() {
		return errors.size() == 0;
	}

	public CRUDContext getCtx() {
		return ctx;
	}

	public void setCtx(CRUDContext ctx) {
		this.ctx = ctx;
	}

	public String getModelClassSimpleName() {
		return modelClassSimpleName;
	}

	public void setModelClassSimpleName(String modelClassSimpleName) {
		this.modelClassSimpleName = modelClassSimpleName;
	}
}
