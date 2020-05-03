package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.individuality.agenda;

import bei7473p5254d69jcuat.tenyu.db.*;
import glb.util.*;
import jetbrains.exodus.env.*;

public interface AgendaContentI extends StorableI {
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
	 * @param holder	このAgendaContentを内包しているAgenda
	 * @return	処理に成功したか
	 * @throws Exception	発生するとトランザクションが破棄される
	 */
	boolean run(Transaction txn, long nextHistoryIndex, Agenda holder) throws Exception;

}