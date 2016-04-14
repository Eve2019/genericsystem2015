package org.genericsystem.distributed.cacheonserver.todocarmvc;

import io.vertx.core.http.ServerWebSocket;

import org.genericsystem.distributed.ui.components.HtmlApp;
import org.genericsystem.distributed.ui.components.HtmlButton;
import org.genericsystem.distributed.ui.components.HtmlDiv;
import org.genericsystem.distributed.ui.components.HtmlFooter;
import org.genericsystem.distributed.ui.components.HtmlH1;
import org.genericsystem.distributed.ui.components.HtmlHeader;
import org.genericsystem.distributed.ui.components.HtmlHyperLink;
import org.genericsystem.distributed.ui.components.HtmlInputText;
import org.genericsystem.distributed.ui.components.HtmlLabel;
import org.genericsystem.distributed.ui.components.HtmlLi;
import org.genericsystem.distributed.ui.components.HtmlSection;
import org.genericsystem.distributed.ui.components.HtmlSpan;
import org.genericsystem.distributed.ui.components.HtmlStrong;
import org.genericsystem.distributed.ui.components.HtmlUl;
import org.genericsystem.kernel.Engine;

public class CarApp extends HtmlApp {

	public CarApp(Engine engine, ServerWebSocket webSocket) {
		super(new CarListModel(engine), webSocket);
	}

	@Override
	protected void initChildren() {

		HtmlDiv div = new HtmlDiv(this);
		{

			HtmlSection carapp = new HtmlSection(div).setStyleClass("todoapp");
			{
				HtmlHeader header = new HtmlHeader(carapp).setStyleClass("header");
				{
					new HtmlH1(header).setText("MyCars");
					new HtmlInputText(header).setStyleClass("new-todo").bindAction(CarListModel::create).bindTextBidirectional(CarListModel::getName);
					// new HtmlInputText(header).setStyleClass("new-todo").bindAction(CarListModel::create).bindTextBidirectional(CarListModel::getPowerValue);
				}

				HtmlSection main = new HtmlSection(carapp).setStyleClass("main");
				{
					HtmlUl carlist = new HtmlUl(main).setStyleClass("todo-list");

					HtmlLi li = new HtmlLi(carlist).forEach(CarListModel::getCarModels);
					{
						HtmlDiv carDiv = new HtmlDiv(li).setStyleClass("view");
						{
							// new HtmlCheckBox(todoDiv).setStyleClass("toggle");
							new HtmlLabel(carDiv).bindText(CarModel::getCarString);
							HtmlDiv powerDiv = new HtmlDiv(carDiv).setStyleClass("view").select(CarModel::getPowerValueModel);
							{
								new HtmlLabel(powerDiv).bindText(PowerValueModel::getPowerValueString);
							}
							new HtmlButton(carDiv).setStyleClass("destroy").bindAction(CarModel::remove);
						}
					}
				}

			}
			HtmlFooter footer = new HtmlFooter(carapp).setStyleClass("footer");
			{
				HtmlSpan span = new HtmlSpan(footer).setStyleClass("todo-count");
				{
					new HtmlStrong(span);
					new HtmlSpan(span);
				}

				HtmlUl filters = new HtmlUl(footer).setStyleClass("filters");
				{
					new HtmlHyperLink(new HtmlLi(filters), "Flush", CarListModel::flush);
					new HtmlHyperLink(new HtmlLi(filters), "Cancel", CarListModel::cancel);

				}

			}
		}
	}
}
