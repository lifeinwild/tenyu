package bei7473p5254d69jcuat.tenyutalk.model.promise;

import bei7473p5254d69jcuat.tenyutalk.model.promise.*;
import bei7473p5254d69jcuat.tenyutalk.model.release1.*;
import bei7473p5254d69jcuat.tenyutalk.reference.*;

/**
 * フォルダは複数のファイルまたはフォルダの特定バージョンを参照する。
 *
 * フォルダの中にフォルダがあるということはツリー構造が可能ということで、
 * それらツリー全体に跨る再帰的処理が提供される。
 *
 * 重要な点としてフォルダやファイルは自身がどのフォルダから参照されているか
 * という情報を持っていない。（検索可能だがオブジェクトの状態として持っていない）
 * フォルダは一方的に他のファイルやフォルダを参照している。
 * フォルダAの中にフォルダBがあり、フォルダBの中にフォルダAがあるという事が可能。
 * この点で再帰構造が可能になっているので再帰的処理を実行する場合無限ループに注意する必要がある。
 * 
 * あるフォルダをローカルのアップロードする
 *
 * ファイルまたはフォルダは{@link CreativeObject}を継承しているので
 * セマンテックバージョニングされていて、
 * {@link TenyutalkReferenceFlexible}を使う事で
 * 同じメジャーバージョンの中で最新のものを選択する等ができる。
 * つまり{@link TenyutalkFolderDBI}は参照先のファイルやフォルダのアップデートが自動的に反映される
 * 柔軟なものにできる。
 * もちろん他の人が作ったファイルやフォルダを参照できる。
 * そこにある多人数参加型の情報構築の仕組みは革新的なものといえる。
 * programmingEnvironment.mdで述べたように、私は最終的にそれを用いて
 * 実行時依存性解決や新しいプログラミング環境を実現する事を想定している。
 *
 * 将来的に予定されている検索サーバによってP2Pネットワーク全体のファイルやフォルダを
 * 複雑な条件で検索できるようになる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyutalkFolderDBI extends CreativeObjectDBI {
}