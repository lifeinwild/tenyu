package bei7473p5254d69jcuat.tenyu.release1.global.middle.takeoverserver.game;

/**
 * チーム申請
 * @author exceptiontenyu@gmail.com
 *
 */
public class TeamApplication {
	/**
	 * 対象チーム
	 */
	private TeamReference tref;

	/**
	 * 申請者
	 */
	private Long applicant;

	public TeamReference getTref() {
		return tref;
	}

	public void setTref(TeamReference tref) {
		this.tref = tref;
	}

	public Long getApplicant() {
		return applicant;
	}

	public void setApplicant(Long applicant) {
		this.applicant = applicant;
	}

}
