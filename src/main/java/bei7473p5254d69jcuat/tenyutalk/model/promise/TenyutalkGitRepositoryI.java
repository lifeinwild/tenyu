package bei7473p5254d69jcuat.tenyutalk.model.promise;

import java.io.*;

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.storage.file.*;

/**
 * 客観の{@link TenyuGitRepositoryI}と対応するノード別管理となる情報。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyutalkGitRepositoryI extends TenyutalkRepositoryI {
	/**
	 * @return 作成されたgitリポジトリ
	 * @throws IOException
	 */
	default Repository createGitRepository() throws IOException {
		return new FileRepositoryBuilder()
				.setGitDir(new File(getTenyuRepositoryWorkingDir() + ".git"))
				.build();
	}

}
