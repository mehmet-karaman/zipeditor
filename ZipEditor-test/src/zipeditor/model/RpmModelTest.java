package zipeditor.model;

import zipeditor.model.ZipContentDescriber.ContentTypeId;

public class RpmModelTest extends AbstractModelTest {

	@Override
	public String getArchiveName() {
		return "archive.rpm";
	}

	@Override
	public ContentTypeId getArchiveType() {
		return ContentTypeId.RPM_FILE;
	}

}
