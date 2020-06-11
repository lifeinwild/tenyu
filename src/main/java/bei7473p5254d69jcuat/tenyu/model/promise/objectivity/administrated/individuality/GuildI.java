package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.sociality.*;

/**
 * ギルド概念。例えば常駐空間ゲームではギルドに経験値やレベルや装備があり成長する。
 *
 * ギルドマスターは立候補及びギルドメンバーの承認で決定する。
 * 承認はいつでも取り消せる。
 *
 * {@link SocialityI}を持つ。
 *
 * どのユーザーも１つ以下のギルドにしか所属できない。
 *
 * ギルドはゲームの制作者になれる。
 *
 * ギルドマスターが脱退した場合、自動的に最も古参のメンバーがギルドマスターになる。
 * その後メンバーの投票でギルドマスターを変える事ができる。
 * この投票はその新しいギルドマスターが一切操作する事無く（幽霊部員でも）できる。
 *
 * 関連：{@link UserI}はただ１つの{@link GuildI}に所属する。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface GuildI extends IndividualityObjectI, HasSocialityI {
	/**
	 * @return	ギルドメンバーのユーザーIDのリスト
	 */
	List<Long> getGuildMemberIds();

}
