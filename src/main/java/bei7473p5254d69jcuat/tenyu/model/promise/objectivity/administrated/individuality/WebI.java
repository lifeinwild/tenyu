package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;

/**
 * Webと名付けるかURLと名付けるか迷ったが、
 * URL証明を実施できるのはHTML等の一部のURLに限られ、
 * 例えば画像ファイルのURL等を対象にできるわけではないので、Webとした。
 * 名前や説明等をつけれるのもURLそのものではなくWebページを対象としているから。
 *
 * これに関連する機能でURL証明というものがあるが、
 * Web証明と呼ぶのが正しいかもしれないが、やや分かりにくい気がするので
 * とりあえず変えずにURL証明と呼ぶ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface WebI extends IndividualityObjectI, HasSocialityI {

	String getUrl();

	@Override
	default int getNameMax() {
		return 2048;
	}

	//void setUrl(String url);
}
