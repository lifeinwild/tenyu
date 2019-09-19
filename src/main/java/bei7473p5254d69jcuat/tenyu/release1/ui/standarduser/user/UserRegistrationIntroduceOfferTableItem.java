package bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.release1.communication.*;
import bei7473p5254d69jcuat.tenyu.release1.communication.request.gui.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.neighbor.*;

public class UserRegistrationIntroduceOfferTableItem
		extends AbstractP2PEdgeAndUserTableItem<Message> {
	private Message message;
	private UserRegistrationIntroduceOffer offer;

	public UserRegistrationIntroduceOfferTableItem(Message message) {
		super(message.getEdgeByInnermostPackage());
		this.message = message;
		offer = (UserRegistrationIntroduceOffer) message.getContent();
		updateUser();
	}

	@Override
	public Message getSrc() {
		return message;
	}

	protected void updateUser() {
		setUserId(IdObjectDBI.getNullId());
		setName(offer.getInfo().getMe().getName());
		setExplanation(offer.getInfo().getMe().getExplanation());
	}

	public Message getMessage() {
		return message;
	}

	public UserRegistrationIntroduceOffer getOffer() {
		return offer;
	}
}
