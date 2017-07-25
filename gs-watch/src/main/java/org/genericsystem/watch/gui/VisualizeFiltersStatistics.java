package org.genericsystem.watch.gui;

import org.genericsystem.common.Root;
import org.genericsystem.cv.comparator.ComputeTrainedScores;
import org.genericsystem.cv.model.Doc;
import org.genericsystem.cv.model.DocClass;
import org.genericsystem.cv.model.ImgFilter;
import org.genericsystem.cv.model.MeanLevenshtein;
import org.genericsystem.cv.model.Score;
import org.genericsystem.cv.model.ZoneGeneric;
import org.genericsystem.cv.model.ZoneText;
import org.genericsystem.reactor.Context;
import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.annotations.BindAction;
import org.genericsystem.reactor.annotations.Children;
import org.genericsystem.reactor.annotations.DependsOnModel;
import org.genericsystem.reactor.annotations.DirectSelect;
import org.genericsystem.reactor.annotations.SetText;
import org.genericsystem.reactor.annotations.Style;
import org.genericsystem.reactor.annotations.Switch;
import org.genericsystem.reactor.appserver.ApplicationServer;
import org.genericsystem.reactor.context.ContextAction;
import org.genericsystem.reactor.gscomponents.AppHeader;
import org.genericsystem.reactor.gscomponents.AppHeader.AppTitleDiv;
import org.genericsystem.reactor.gscomponents.AppHeader.Logo;
import org.genericsystem.reactor.gscomponents.HtmlTag.HtmlButton;
import org.genericsystem.reactor.gscomponents.HtmlTag.HtmlH1;
import org.genericsystem.reactor.gscomponents.InstancesTable;
import org.genericsystem.reactor.gscomponents.RootTagImpl;
import org.genericsystem.watch.VerticleDeployerFromWatchApp;
import org.genericsystem.watch.gui.PageSwitcher.FILTERS_STATISTICS;
import org.genericsystem.watch.gui.VisualizeFiltersStatistics.RunScriptButton;

import io.vertx.core.Verticle;

/**
 * 
 * 
 * @author Pierrik Lassalas
 *
 */
// TODO: redesign the interface (smaller, add foreach loops)
@Switch(FILTERS_STATISTICS.class)
@DependsOnModel({ Doc.class, DocClass.class, ZoneGeneric.class, ZoneText.class, ImgFilter.class, Score.class, MeanLevenshtein.class })
@Style(name = "background-color", value = "#ffffff")
@Children({ AppHeader.class, InstancesTable.class })
@Style(path = AppHeader.class, name = "background-color", value = "#00afeb")
@Style(path = InstancesTable.class, name = "width", value = "90%")
@Style(path = InstancesTable.class, name = "margin", value = "auto")
@Children(path = AppHeader.class, value = { Logo.class, AppTitleDiv.class, RunScriptButton.class })
@SetText(path = { AppHeader.class, AppTitleDiv.class, HtmlH1.class }, value = "Global OCR accuracy per zone")
@DirectSelect(path = InstancesTable.class, value = Score.class)
public class VisualizeFiltersStatistics extends RootTagImpl {

	public static void main(String[] mainArgs) {
		ApplicationServer.startSimpleGenericApp(mainArgs, VisualizeFiltersStatistics.class, "/gs-cv_model3");
	}

	@Override
	public void init() {
		createNewInitializedProperty(PageSwitcher.PAGE, c -> PageSwitcher.FILTERS_STATISTICS);
	}

	@SetText("Compute statistics")
	@BindAction({ COMPUTE_STATS.class })
	public static class RunScriptButton extends HtmlButton {

	}

	public static class COMPUTE_STATS implements ContextAction {
		@Override
		public void accept(Context context, Tag tag) {
			System.out.println("Computing scores...");
			DocClassInstance docClassInstance = (DocClassInstance) context.getGeneric();
			Root root = docClassInstance.getRoot();
			Arrays.asList(context.getGenerics()).forEach(g -> System.out.println(g.info()));
			Verticle worker = new WorkerVerticle() {
				@Override
				public void start() throws Exception {
					ComputeTrainedScores.compute(root, docClassInstance.getValue().toString());
					System.out.println("Done computing scores!");
				}
			};
			VerticleDeployerFromWatchApp.deployWorkerVerticle(worker, "Failed to execute the task");
		}
	}

}