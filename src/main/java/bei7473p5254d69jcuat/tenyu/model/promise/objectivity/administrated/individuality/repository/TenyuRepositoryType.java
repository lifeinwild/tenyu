package bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository;

import glb.*;

/**
 * {@link TenyuRepositoryI}の種類。
 * 種類によってリポジトリのフォルダアーキテクチャ等が変わる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public enum TenyuRepositoryType {
	/**
	 * {@link FileManagement#getTenyutalkRepositoryWorkingDir(TenyuRepositoryI)}内に
	 * .gitフォルダが置かれ、gitローカルリポジトリが作成されるタイプのリポジトリ。
	 *
	 * この種類のリポジトリによって制作活動をサポートする。
	 *
	 * ここに作成されるgitローカルリポジトリはgithub等にアップロードできる。
	 * ただし複数のソフトウェアから同時にgitローカルリポジトリを操作すると
	 * 問題が生じるかもしれない。
	 *
	 */
	GIT,;
}
