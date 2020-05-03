package bei7473p5254d69jcuat.tenyu.model.release1.middle.takeoverserver.game;

import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;

/**
 * name+gameRefをMapのキーにするため作成した
 * @author exceptiontenyu@gmail.com
 *
 */
public class TeamReference {
	private String name;
	private int teamClassId;
	private Long ratingGameId;

	public boolean validate() {
		if (name == null)
			return false;
		if (teamClassId < 0)
			return false;
		if (ratingGameId == null
				|| !Model.validateIdStandardNotSpecialId(ratingGameId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TeamReference [name=" + name + ", teamClassId=" + teamClassId
				+ ", ratingGameId=" + ratingGameId + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((ratingGameId == null) ? 0 : ratingGameId.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (ratingGameId == null) {
			if (other.ratingGameId != null)
				return false;
		} else if (!ratingGameId.equals(other.ratingGameId))
			return false;
		if (teamClassId != other.teamClassId)
			return false;
		return true;
	}

	public Long getRatingGameId() {
		return ratingGameId;
	}

	public void setRatingGameId(Long ratingGameId) {
		this.ratingGameId = ratingGameId;
	}

}