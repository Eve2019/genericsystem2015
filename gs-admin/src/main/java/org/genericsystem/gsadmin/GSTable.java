package org.genericsystem.gsadmin;

import java.util.function.Function;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.HBox;

import org.genericsystem.gsadmin.GenericRowBuilders.TableCellRowBuilder;
import org.genericsystem.gsadmin.GenericRowBuilders.TextCellFirstRowBuilder;
import org.genericsystem.gsadmin.GenericRowBuilders.TextCellRowBuilder;
import org.genericsystem.gsadmin.GenericTableBuilders.TableCellTableBuilder;
import org.genericsystem.ui.Element;
import org.genericsystem.ui.components.GSHBox;
import org.genericsystem.ui.components.GSSCrollPane;
import org.genericsystem.ui.components.GSVBox;
import org.genericsystem.ui.table.Row;
import org.genericsystem.ui.table.Table;
import org.genericsystem.ui.table.Window;

public abstract class GSTable extends GSVBox {

	public GSTable(Element<?> parent) {
		super(parent);
	}

	@Override
	protected void initChildren() {
		GSSCrollPane scrollPane = new GSSCrollPane(this).setStyleClass("scrollable");
		{
			GSVBox tablePanel = new GSVBox(scrollPane).setStyleClass(Table::getStyleClass).setSuperPrefWidth(getSuperPrefWidth()).setSuperPrefHeight(getSuperPrefHeight());
			{
				new GSHBox(tablePanel).select(Table::getFirstElement).include(new TextCellFirstRowBuilder<>()::init).setStyleClass(Row::getStyleClass).setMinHeight(Table::getFirstRowHeight).setMaxHeight(Table::getFirstRowHeight)
						.setPrefHeight(Table::getFirstRowHeight);
				createSelectionHBox(tablePanel).forEach(Table::getElements).setStyleClass(Row::getStyleClass).setMinHeight(Table::getRowHeight).setMaxHeight(Table::getRowHeight).setPrefHeight(Table::getRowHeight);
			}
		}
	}

	protected abstract GSHBox createSelectionHBox(Element<?> parent);

	protected abstract <M> Function<M, ObservableValue<Number>> getSuperPrefWidth();

	protected abstract <M> Function<M, ObservableValue<Number>> getSuperPrefHeight();

	public static class GSTextCellTable extends GSTable {

		public GSTextCellTable(Element<?> parent) {
			super(parent);
		}

		@Override
		protected <M> Function<M, ObservableValue<Number>> getSuperPrefWidth() {
			return table -> ((Table) table).getColumnWidth();
		}

		@Override
		protected <M> Function<M, ObservableValue<Number>> getSuperPrefHeight() {
			return table -> ((Table) table).getRowHeight();
		}

		@Override
		public GSHBox createSelectionHBox(Element<?> parent) {
			return (GSHBox) new GSHBox(parent).include(new TextCellRowBuilder<>()::init).addBindMetaAction(HBox::onMouseClickedProperty, GenericWindow::selectRowGenericTable);
		}
	}

	public static class GSTableCellTable extends GS {
		@Override
		public GSHBox createSelectionHBox(Element<?> parent) {
			return (GSHBox) new GSHBox(parent).include(new TableCellRowBuilder<>()::init).addBindMetaAction(HBox::onMouseClickedProperty, GenericWindow::selectRowEngineTable);
		}

		@Override
		protected <M> Function<M, ObservableValue<Number>> getSuperPrefWidth() {
			return app -> new SimpleObjectProperty<>(900);
		}

		@Override
		protected <M> Function<M, ObservableValue<Number>> getSuperPrefHeight() {
			return app -> ((Window) app).getHeight();
		}
	}

	public static class EditTableCellTableBuilder extends TableCellTableBuilder {
		@Override
		public GSHBox createSelectionHBox(Element<?> parent) {
			return (GSHBox) new GSHBox(parent).include(new TableCellRowBuilder<>()::init).addBindMetaAction(HBox::onMouseClickedProperty, GenericCrud::test);
		}
	}

	public static class TableCellTableBuilder2 extends TableCellTableBuilder {
		@Override
		public GSHBox createSelectionHBox(Element<?> parent) {
			return (GSHBox) new GSHBox(parent).include(new TableCellRowBuilder<>()::init).addBindMetaAction(HBox::onMouseClickedProperty, GenericWindow::selectRowGenericTable);
		}
	}

}
