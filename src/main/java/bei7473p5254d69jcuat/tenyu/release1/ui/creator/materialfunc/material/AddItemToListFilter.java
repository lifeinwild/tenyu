package bei7473p5254d69jcuat.tenyu.release1.ui.creator.materialfunc.material;

import java.nio.file.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.objectivity.naturality.file.*;

/**
 * 指定されたファイルまたはフォルダより下にある全ファイルを再帰的に中間リストに登録する
 *
 * TODO:このコードはMaterial限定になっているし、Files.walk等も検討していなかった。
 *
 * @param built1
 * @param built2
 * @param start		再帰的探索の開始位置
 * @param here		現在の探索位置
 */
/*
public static boolean getFilesFromDirRecursive(MaterialGui built1,
		MaterialListGui built2, String dir, File here,
		BiFunction<AddItemToListFilter, Transaction, Boolean> filter,
		ValidationResult r) throws Exception {
	if (!here.exists()) {
		r.add(Lang.MATERIAL, Lang.ERROR_FILE_NOT_FOUND);
		return false;
	}
	if (here.isFile()) {
		try {
			String relativePath = dir + here.getName();
			if (!addItemToList(here.toPath(), relativePath,
					built1.getExplanationInput().getText(), here.length(),
					built1.getUserLimitInput().getText(), built2, filter,
					r))
				return false;
		} catch (Exception e) {
			Glb.debug(e);
			String path = "";
			if (here != null && here.getAbsolutePath() != null)
				path = here.getAbsolutePath();
			r.add(Lang.MATERIAL, Lang.ERROR_FILE_NOT_FOUND, " " + path);
			return false;
		}
	} else if (here.isDirectory()) {
		String sepa = Glb.getConst().getFileSeparator();
		for (File c : here.listFiles()) {
			if (!getFilesFromDirRecursive(built1, built2,
					dir + here.getName() + sepa, c, filter, r))
				return false;
		}
	}
	return true;
}
*/
public class AddItemToListFilter {
	private Material src;
	private ValidationResult r;
	private Path path;

	public AddItemToListFilter(Material src, ValidationResult r,
			Path path) {
		this.src = src;
		this.r = r;
		this.path = path;
	}

	public Material getSrc() {
		return src;
	}

	public ValidationResult getR() {
		return r;
	}

	public Path getPath() {
		return path;
	}
}