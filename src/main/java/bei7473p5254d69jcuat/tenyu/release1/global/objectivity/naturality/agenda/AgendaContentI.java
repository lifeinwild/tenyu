package bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.agenda;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

public interface AgendaContentI extends Storable {
	@Override
	default boolean validateAtUpdate(ValidationResult r) {
		//更新は無いのでここで仮の実装を与える
		return validateAtCreate(r);
	}

	@Override
	default boolean validateAtDelete(ValidationResult r) {
		return true;
	}

	/**
	 * 可決された場合に呼び出される。
	 * @param a	このAgendaContentを内包しているAgenda
	 * @return	処理に成功したか
	 * @throws Exception	発生するとトランザクションが破棄される
	 */
	boolean run(Agenda a) throws Exception;

}