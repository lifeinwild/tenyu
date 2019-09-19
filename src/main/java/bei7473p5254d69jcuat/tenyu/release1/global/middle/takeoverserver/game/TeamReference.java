package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.statebyuser.*;

/**
 * name+gameRefをMapのキーにするため作成した
 * @author exceptiontenyu@gmail.com
 *
 */
public class TeamReference {
	private String name;
	private int teamClassId;
	private GameReference gameRef;

	public boolean validate() {
		if (name == null)
			return false;
		if (teamClassId < 0)
			return false;
		if (gameRef == null
				|| !IdObject.validateIdStandardNotSpecialId(gameRef.getGameId())
				|| gameRef.getType() == null)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return " teamClassId=" + teamClassId + " name=" + name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GameReference getGameRef() {
		return gameRef;
	}

	public void setGameRef(GameReference gameRef) {
		this.gameRef = gameRef;
	}

	public int getTeamClassId() {
		return teamClassId;
	}

	public void setTeamClassId(int teamClassId) {
		this.teamClassId = teamClassId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameRef == null) ? 0 : gameRef.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + teamClassId;
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
		TeamReference other = (TeamReference) obj;
		if (gameRef == null) {
			if (other.gameRef != null)
				return false;
		} else if (!gameRef.equals(other.gameRef))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (teamClassId != other.teamClassId)
			return false;
		return true;
	}

}