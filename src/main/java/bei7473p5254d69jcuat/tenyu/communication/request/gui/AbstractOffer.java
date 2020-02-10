package bei7473p5254d69jcuat.tenyu.communication.request.gui;

import bei7473p5254d69jcuat.tenyu.communication.*;

/**
 * メッセージ作成者→承認者→受付サーバとなるタイプの
 * 承認者が受け取るメッセージ。
 * 一旦他のユーザーを経由して受付サーバに届けられるメッセージの経由段階のメッセージ
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public abstract class AbstractOffer extends GuiCausedRequest {
	protected abstract boolean validateAbstractOfferConcrete(Message m);

	@Override
	protected boolean validateGuiCausedConcrete(Message m) {
		return validateAbstractOfferConcrete(m);
	}
}
