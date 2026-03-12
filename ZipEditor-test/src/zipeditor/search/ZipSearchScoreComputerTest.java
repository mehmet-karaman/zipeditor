package zipeditor.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchPageScoreComputer;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.junit.Before;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class ZipSearchScoreComputerTest {

	private ZipSearchScoreComputer computer;

	@Before
	public void before() {
		computer = new ZipSearchScoreComputer();
	}

	@Test
	public void shouldScoreHighForNode() throws Exception {
		File path = new File("resources/archive.zip");
		ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		Node node = model.findNode("about.html");

		assertEquals(1, computer.computeScore(ZipSearchPage.ID, node));
	}

	@Test
	public void shouldScoreLowestForWrongPageId() throws Exception {
		File path = new File("resources/archive.zip");
		ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		Node node = model.findNode("about.html");

		assertEquals(ISearchPageScoreComputer.LOWEST, computer.computeScore("some.other.page", node));
	}

	@Test
	public void shouldScoreLowestForUnknownInput() {
		assertEquals(ISearchPageScoreComputer.LOWEST, computer.computeScore(ZipSearchPage.ID, "a string"));
	}

	@Test
	public void shouldScoreHighForIFile() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("scoreComputerTestProject");
		try {
			if (!project.exists())
				project.create(new NullProgressMonitor());
			project.open(new NullProgressMonitor());
			File archiveFile = new File("resources/archive.zip").getAbsoluteFile();
			IFile file = project.getFile("archive.zip");
			if (!file.exists())
				file.createLink(new Path(archiveFile.getAbsolutePath()), 0, new NullProgressMonitor());

			int score = computer.computeScore(ZipSearchPage.ID, file);
			assertTrue("Score for zip IFile should be > LOWEST", score > ISearchPageScoreComputer.LOWEST);
		} finally {
			project.delete(true, new NullProgressMonitor());
		}
	}

	@Test
	public void shouldScoreHighForURIEditorInput() {
		IURIEditorInput input = new IURIEditorInput() {
			public URI getURI() {
				return new File("resources/archive.zip").toURI();
			}
			public String getName() {
				return "archive.zip";
			}
			public String getToolTipText() {
				return "";
			}
			public ImageDescriptor getImageDescriptor() {
				return null;
			}
			public boolean exists() {
				return true;
			}
			public IPersistableElement getPersistable() {
				return null;
			}
			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}
		};
		assertEquals(1, computer.computeScore(ZipSearchPage.ID, input));
	}

	@Test
	public void shouldScoreZeroForNonZipURIEditorInput() {
		IURIEditorInput input = new IURIEditorInput() {
			public URI getURI() {
				return new File("resources/test.txt").toURI();
			}
			public String getName() {
				return "test.txt";
			}
			public String getToolTipText() {
				return "";
			}
			public ImageDescriptor getImageDescriptor() {
				return null;
			}
			public boolean exists() {
				return true;
			}
			public IPersistableElement getPersistable() {
				return null;
			}
			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}
		};
		assertEquals(0, computer.computeScore(ZipSearchPage.ID, input));
	}

	@Test
	public void shouldScoreHighForFileEditorInput() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("scoreComputerTestProject2");
		try {
			if (!project.exists())
				project.create(new NullProgressMonitor());
			project.open(new NullProgressMonitor());
			File archiveFile = new File("resources/archive.zip").getAbsoluteFile();
			final IFile file = project.getFile("archive.zip");
			if (!file.exists())
				file.createLink(new Path(archiveFile.getAbsolutePath()), 0, new NullProgressMonitor());

			IFileEditorInput input = new IFileEditorInput() {
				public IFile getFile() {
					return file;
				}
				public String getName() {
					return file.getName();
				}
				public String getToolTipText() {
					return "";
				}
				public ImageDescriptor getImageDescriptor() {
					return null;
				}
				public boolean exists() {
					return true;
				}
				public IPersistableElement getPersistable() {
					return null;
				}
				public <T> T getAdapter(Class<T> adapter) {
					return null;
				}
				public IStorage getStorage() {
					return null;
				}
			};
			int score = computer.computeScore(ZipSearchPage.ID, input);
			assertTrue("Score for IFileEditorInput should be > LOWEST", score > ISearchPageScoreComputer.LOWEST);
		} finally {
			project.delete(true, new NullProgressMonitor());
		}
	}

	@Test
	public void shouldScoreHighForStorageEditorInput() {
		IStorageEditorInput input = new IStorageEditorInput() {
			public IStorage getStorage() {
				return new IStorage() {
					public InputStream getContents() {
						return null;
					}
					public IPath getFullPath() {
						return new Path("resources/archive.zip");
					}
					public String getName() {
						return "archive.zip";
					}
					public boolean isReadOnly() {
						return true;
					}
					public <T> T getAdapter(Class<T> adapter) {
						return null;
					}
				};
			}
			public String getName() {
				return "archive.zip";
			}
			public String getToolTipText() {
				return "";
			}
			public ImageDescriptor getImageDescriptor() {
				return null;
			}
			public boolean exists() {
				return true;
			}
			public IPersistableElement getPersistable() {
				return null;
			}
			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}
		};
		assertEquals(1, computer.computeScore(ZipSearchPage.ID, input));
	}

	@Test
	public void shouldScoreHighForPathEditorInput() {
		IPathEditorInput input = new IPathEditorInput() {
			public IPath getPath() {
				return new Path("resources/archive.zip");
			}
			public String getName() {
				return "archive.zip";
			}
			public String getToolTipText() {
				return "";
			}
			public ImageDescriptor getImageDescriptor() {
				return null;
			}
			public boolean exists() {
				return true;
			}
			public IPersistableElement getPersistable() {
				return null;
			}
			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}
		};
		assertEquals(1, computer.computeScore(ZipSearchPage.ID, input));
	}
}
