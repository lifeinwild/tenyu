package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import javafx.beans.property.*;

public class MaterialTableItem
		extends NaturalityTableItem<MaterialDBI, Material>
		implements TableRow<Material> {
	private UploadFileGui info;
	private LongProperty fileSize = new SimpleLongProperty();
	private StringProperty userLimit = new SimpleStringProperty();

	public MaterialTableItem(Material src, UploadFileGui info) {
		super(src);
		this.info = info;
		updateMaterialTableItem();
	}

	public UploadFileGui getInfo() {
		return info;
	}

	@Override
	public Material getSrc() {
		return src;
	}

	public void updateMaterialTableItem() {
		setFileSize(src.getFileSize());
		if (src.getUserLimitation() != null) {
			StringBuilder sb = new StringBuilder();
			for (Long id : src.getUserLimitation()) {
				User u = Glb.getObje().getUser(us -> us.get(id));
				if (u == null)
					continue;
				sb.append(u.getName() + " ");
			}
			this.userLimit.set(sb.toString());
		}
	}

	@Override
	public void update() {
		super.update();
	}

	public Long getFileSize() {
		return fileSize.get();
	}

	public String getUserLimit() {
		return userLimit.get();
	}

	public void setFileSize(long fileSize) {
		this.fileSize.set(fileSize);
	}

	public void setFileSize(LongProperty fileSize) {
		this.fileSize = fileSize;
	}

	public void setUserLimit(StringProperty userLimit) {
		this.userLimit = userLimit;
	}

}