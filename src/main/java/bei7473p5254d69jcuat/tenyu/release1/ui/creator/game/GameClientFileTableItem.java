package bei7473p5254d69jcuat.tenyu.release1.ui.creator.game;

import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.game.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material.*;
import javafx.beans.property.*;

public class GameClientFileTableItem
		implements TableRow<TenyuFile> {
	private TenyuFile src;
	private LongProperty fileSize = new SimpleLongProperty();
	private StringProperty relativeFilePath = new SimpleStringProperty();
	private UploadFileGui uploadInfo;

	public GameClientFileTableItem(TenyuFile src,
			UploadFileGui uploadInfo) {
		this.src = src;
		this.uploadInfo = uploadInfo;
		update();
	}

	public Long getFileSize() {
		return fileSize.get();
	}

	public void setFileSize(long fileSize) {
		this.fileSize.set(fileSize);
	}

	public String getRelativeFilePath() {
		return relativeFilePath.get();
	}

	public void setRelativeFilePath(String relativeFilePath) {
		this.relativeFilePath.set(relativeFilePath);
	}

	@Override
	public TenyuFile getSrc() {
		return src;
	}

	@Override
	public void update() {
		setFileSize(src.getFileSize());
		setRelativeFilePath(src.getRelativePathStr());
	}

	public UploadFileGui getUploadInfo() {
		return uploadInfo;
	}

	public void setUploadInfo(UploadFileGui uploadInfo) {
		this.uploadInfo = uploadInfo;
	}

	public void setSrc(TenyuFile src) {
		this.src = src;
	}

	public void setFileSize(LongProperty fileSize) {
		this.fileSize = fileSize;
	}

	public void setRelativeFilePath(StringProperty relativeFilePath) {
		this.relativeFilePath = relativeFilePath;
	}

}