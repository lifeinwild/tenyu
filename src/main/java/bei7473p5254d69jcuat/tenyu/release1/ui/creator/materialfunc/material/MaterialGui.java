package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import java.nio.file.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.db.*;
import bei7473p5254d69jcuat.tenyu.release1.db.store.file.*;
import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.common.*;
import bei7473p5254d69jcuat.tenyu.release1.ui.standarduser.user.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import jetbrains.exodus.env.*;

public class MaterialGui extends
		NaturalityGui<MaterialDBI,
				Material,
				Material,
				MaterialStore,
				MaterialGui,
				MaterialTableItem> {
	public MaterialGui(String name, String id) {
		super(name, id);
	}

	@Override
	protected MaterialGui createDetailGui() {
		return new MaterialGui(Lang.DETAIL.toString(), idPrefix + detail);
	}

	@Override
	protected MaterialTableItem createTableItem(Material o) {
		return new MaterialTableItem(o, null);
	}

	private SubmitButtonGui limitAddButton;
	private Path selectedFile;

	private UserGui userLimit;
	private TableView<UserTableItem> userLimitTable;

	public UserGui getUserLimit() {
		return userLimit;
	}

	public void setUserLimit(UserGui userLimit) {
		this.userLimit = userLimit;
	}

	@Override
	public GridPane buildSearch(SearchFuncs<MaterialDBI, Material> sf) {
		//総件数
		buildCount(DBUtil.countStatic(MaterialStore.getMainStoreInfoStatic()));

		super.buildSearch(null);

		add(detailGui);

		return getGrid();
	}

	@Override
	public GridPane buildDelete(Material exist) {
		super.buildDelete(exist);

		buildSubmitButton(gui -> {
			return exist != null;
		}, gui -> {
			//TODO
			return true;
		}, gui -> clear(), null);
		elapsed += 1;

		return grid;

	}

	@Override
	public void clear() {
		super.clear();
		selectedFile = null;
		userLimitTable.getItems().clear();
		userLimit.clear();
	}

	@Override
	public Lang getClassNameLang() {
		return Lang.MATERIAL;
	}

	public SubmitButtonGui getLimitAddButton() {
		return limitAddButton;
	}

	public Path getSelectedFile() {
		return selectedFile;
	}

	public List<Long> getUserLimitIds() {
		if (userLimitTable == null)
			return null;
		List<Long> r = new ArrayList<>();
		for (UserTableItem e : userLimitTable.getItems()) {
			r.add(e.getId());
		}
		return r;
	}

	public TableView<UserTableItem> getUserLimitTable() {
		return userLimitTable;
	}

	@Override
	public void set(Material o) {
		clear();
		super.set(o);
		if (userLimitTable != null) {
			for (Long userId : o.getUserLimitation()) {
				User u = Glb.getObje().getUser(us->us.get(userId));
				if (u == null)
					continue;
				userLimitTable.getItems().add(new UserTableItem(u));
			}
		}
	}

	public void setLimitAddButton(SubmitButtonGui limitAddButton) {
		this.limitAddButton = limitAddButton;
	}

	public void setSelectedFile(Path selectedFile) {
		this.selectedFile = selectedFile;
	}

	public void setUserLimitTable(TableView<UserTableItem> userLimitTable) {
		this.userLimitTable = userLimitTable;
	}

	@Override
	public MaterialStore getStore(Transaction txn) {
		try {
			return new MaterialStore(txn);
		} catch (Exception e) {
			Glb.getLogger().error("", e);
			return null;
		}
	}

}