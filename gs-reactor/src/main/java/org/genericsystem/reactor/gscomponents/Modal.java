package org.genericsystem.reactor.gscomponents;

import org.genericsystem.reactor.annotations.Attribute;
import org.genericsystem.reactor.annotations.BindAction;
import org.genericsystem.reactor.annotations.Children;
import org.genericsystem.reactor.annotations.SelectContext;
import org.genericsystem.reactor.annotations.SetText;
import org.genericsystem.reactor.annotations.Style;
import org.genericsystem.reactor.annotations.Style.FlexDirectionStyle;
import org.genericsystem.reactor.annotations.StyleClass;
import org.genericsystem.reactor.context.ContextAction.CANCEL;
import org.genericsystem.reactor.context.ContextAction.DISPLAY_NONE;
import org.genericsystem.reactor.context.ContextAction.FLUSH;
import org.genericsystem.reactor.context.ContextAction.RESET_SELECTION;
import org.genericsystem.reactor.context.ContextAction.UNMOUNT;
import org.genericsystem.reactor.context.OptionalContextSelector.SELECTION_SELECTOR;
import org.genericsystem.reactor.contextproperties.SelectionDefaults;
import org.genericsystem.reactor.gscomponents.DivWithTitle.TitledInstanceEditor;
import org.genericsystem.reactor.gscomponents.HtmlTag.HtmlButton;
import org.genericsystem.reactor.gscomponents.HtmlTag.HtmlHyperLink;

import javafx.beans.binding.Bindings;

@Children(FlexDiv.class)
@Children(path = FlexDiv.class, value = HtmlHyperLink.class)
@StyleClass("modal")
@StyleClass(path = FlexDiv.class, value = { "widthResponsive", "modal-content" })
@StyleClass(path = { FlexDiv.class, HtmlHyperLink.class }, value = "close")
@Attribute(path = { FlexDiv.class, HtmlHyperLink.class }, name = "name", value = "close")
@Style(path = FlexDiv.class, name = "overflow", value = "auto")
@Style(path = FlexDiv.class, name = "padding", value = "10px")
@Style(path = FlexDiv.class, name = "border-radius", value = "10px")
@Style(path = FlexDiv.class, name = "background-color", value = "white")
@SetText(path = { FlexDiv.class, HtmlHyperLink.class }, value = "×")
public class Modal extends FlexDiv {

	@Children(path = FlexDiv.class, value = { TitledInstanceEditor.class, FlexDiv.class })
	@Children(path = { FlexDiv.class, FlexDiv.class }, pos = { 0, 1 }, value = { HtmlButton.class, HtmlButton.class })
	@SetText(path = { FlexDiv.class, FlexDiv.class, HtmlButton.class }, value = { "Ok", "Cancel" })
	// @Style(path = { FlexDiv.class, TitledInstanceEditor.class }, name = "min-height", value = "300px")
	@Style(path = FlexDiv.class, name = "flex", value = "1 1 0%")
	@Style(path = FlexDiv.class, name = "min-height", value = "350px")
	@Style(path = FlexDiv.class, name = "max-height", value = "50%")
	@Style(path = { FlexDiv.class, FlexDiv.class, HtmlButton.class }, pos = { 0, 1, 0 }, name = "flex", value = "1 1 auto")
	@Style(path = { FlexDiv.class, FlexDiv.class, HtmlButton.class }, pos = { 0, 1, 1 }, name = "flex", value = "1 1 auto")
	@Attribute(path = { FlexDiv.class, FlexDiv.class, HtmlButton.class }, pos = { 0, 1, 1 }, name = "name", value = "close")
	@FlexDirectionStyle(path = { FlexDiv.class, FlexDiv.class }, pos = { 0, 1 }, value = FlexDirection.ROW)
	@SelectContext(path = { FlexDiv.class, TitledInstanceEditor.class }, value = SELECTION_SELECTOR.class)
	@BindAction(path = { FlexDiv.class, FlexDiv.class, HtmlButton.class }, pos = { 0, 1, 0 }, value = { FLUSH.class, UNMOUNT.class, RESET_SELECTION.class })
	@BindAction(path = { FlexDiv.class, FlexDiv.class, HtmlButton.class }, pos = { 0, 1, 1 }, value = { CANCEL.class, UNMOUNT.class, RESET_SELECTION.class })
	public static class ModalEditor extends Modal implements SelectionDefaults {
		@Override
		public void init() {
			bindStyle(DISPLAY, DISPLAY, model -> Bindings.createStringBinding(() -> getSelectionProperty(model).getValue() != null ? "flex" : "none", getSelectionProperty(model)));
		}
	}

	@BindAction(path = { FlexDiv.class, HtmlHyperLink.class }, value = DISPLAY_NONE.class)
	public static class ModalWithDisplay extends Modal {
		@Override
		public void init() {
			createInitializedDisplayProperty("none");
			bindStyle(DISPLAY, DISPLAY);
		}
	}
}
