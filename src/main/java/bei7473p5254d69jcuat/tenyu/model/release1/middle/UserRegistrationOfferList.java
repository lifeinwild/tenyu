package bei7473p5254d69jcuat.tenyu.model.release1.middle;

import java.util.*;
import java.util.concurrent.*;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.*;

/**
 * ユーザー登録の紹介依頼の受付
 * @author exceptiontenyu@gmail.com
 *
 */
public class UserRegistrationOfferList {
	private List<Message> offers = new CopyOnWriteArrayList<>();

	public boolean receive(Message validatedUserRegistration) {
		if (!(validatedUserRegistration
				.getContent() instanceof UserRegistrationIntroduceOffer)) {
			return false;
		}
		return offers.add(validatedUserRegistration);
	}

	public List<Message> getUserRegistrations() {
		return Collections.unmodifiableList(offers);
	}

	public boolean remove(Message m) {
		return offers.remove(m);
	}

	/**
	 * userRegistrationsから当該紹介依頼を削除する
	 * @param userRegistration
	 */
	public void takeReport(Message userRegistration) {
		offers.remove(userRegistration);
	}
}
