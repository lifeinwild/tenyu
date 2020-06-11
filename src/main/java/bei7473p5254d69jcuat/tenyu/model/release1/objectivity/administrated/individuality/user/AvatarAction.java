package bei7473p5254d69jcuat.tenyu.model.release1.objectivity.administrated.individuality.user;

/**
 * アバター動作
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public enum AvatarAction {
	FRONT_2D,//下を向いてる
	BACK_2D,//上を向いてる
	LEFT_2D,
	RIGHT_2D,
	TURN_TO_FRONT_FROM_BACK,
	TURN_TO_FRONT_FROM_RIGHT,
	TURN_TO_FRONT_FROM_LEFT,
	TURN_TO_BACK_FROM_BACK,
	TURN_TO_BACK_FROM_RIGHT,
	TURN_TO_BACK_FROM_LEFT,
	TURN_TO_LEFT,
	TURN_TO_RIGHT,
	FRONT_MOVE,
	BACK_MOVE,
	LEFT_MOVE,
	RIGHT_MOVE,
	FRONT_MOVE_FAST,
	BACK_MOVE_FAST,
	LEFT_MOVE_FAST,
	RIGHT_MOVE_FAST,
	FLY, //飛行モーション系
	JUMP,
	FALL,
	LANDING,
	RIGHT_WEAPON_SHAKE,
	LEFT_SHIELD_DEFENSE,
	SIT_DOWN,//座り
	DOWN,//倒れ
	;

}