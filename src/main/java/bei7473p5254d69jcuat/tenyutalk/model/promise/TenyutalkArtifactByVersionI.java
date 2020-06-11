package bei7473p5254d69jcuat.tenyutalk.model.promise;

import com.github.zafarkhaja.semver.*;

import bei7473p5254d69jcuat.tenyu.model.promise.*;
import bei7473p5254d69jcuat.tenyu.model.promise.objectivity.administrated.individuality.repository.*;
import bei7473p5254d69jcuat.tenyutalk.file.*;
import glb.*;

/**
 * {@link TenyuArtifactByVersionI}に対応するTenyutalk側インターフェース
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public interface TenyutalkArtifactByVersionI extends ObjectI {
	default Version getVersion() {
		return getTenyuArtifactByVersion().getSemVer();
	}

	default TenyuArtifactByVersionI getTenyuArtifactByVersion() {
		return Glb.getObje().getTenyuArtifactByVersion(
				tabvs -> tabvs.get(getTenyuArtifactByVersionId()));
	}

	Long getTenyuArtifactByVersionId();

	/**
	 * @return	成果物
	 */
	default TenyutalkFileMetadataI getTenyutalkFileMetadataI() {
		return getTenyuArtifactByVersion().getFileMetadata();
	}

	/**
	 * @return	アップロード者署名
	 */
	byte[] getUploaderSign();
}
