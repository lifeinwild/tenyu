package bei7473p5254d69jcuat.tenyu.ui.standarduser.user;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.request.gui.*;
import bei7473p5254d69jcuat.tenyu.db.*;
import bei7473p5254d69jcuat.tenyu.db.store.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.*;
import bei7473p5254d69jcuat.tenyu.ui.standarduser.neighbor.*;
import glb.*;

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
		setUserId(ModelI.getNullId());
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
