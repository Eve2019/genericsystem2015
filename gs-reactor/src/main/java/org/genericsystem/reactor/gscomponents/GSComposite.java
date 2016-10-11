package org.genericsystem.reactor.gscomponents;

import org.genericsystem.reactor.Context;
import org.genericsystem.reactor.HtmlDomNode;
import org.genericsystem.reactor.ReactorStatics;
import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.model.StringExtractor;

import org.genericsystem.reactor.modelproperties.SelectionDefaults;

import org.genericsystem.reactor.htmltag.HtmlH1;
import org.genericsystem.reactor.htmltag.HtmlLabel;
import org.genericsystem.reactor.htmltag.HtmlRadio;

import io.vertx.core.json.JsonObject;

/**
 * @author Nicolas Feybesse
 *
 */
public class GSComposite extends GSDiv {

	public GSComposite(Tag parent) {
		this(parent, FlexDirection.COLUMN);
	}

	public GSComposite(Tag parent, FlexDirection flexDirection) {
		super(parent, flexDirection);
		header();
		sections();
		footer();
	}

	protected void header() {

	}

	protected void sections() {
		new GSDiv(this, GSComposite.this.getReverseDirection()) {
			{
				forEach(GSComposite.this);
				new HtmlLabel(this) {
					{
						bindText();
					}
				};
			}
		};
	}

	protected void footer() {
	}

	public static class TitleCompositeFlexElement extends GSComposite {

		public TitleCompositeFlexElement(Tag parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
		}

		public TitleCompositeFlexElement(Tag parent) {
			this(parent, FlexDirection.COLUMN);
		}

		@Override
		protected void header() {
			new GSDiv(this, FlexDirection.ROW) {
				{
					addStyle("justify-content", "center");
					addStyle("background-color", "#ffa500");
					new HtmlH1(this) {
						{
							setStringExtractor(StringExtractor.MANAGEMENT);
							bindText();
						}
					};
				};
			};
		}
	}

	public static class ColorTitleCompositeFlexElement extends TitleCompositeFlexElement {

		public ColorTitleCompositeFlexElement(Tag parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
		}

		public ColorTitleCompositeFlexElement(Tag parent) {
			this(parent, FlexDirection.COLUMN);
		}

		@Override
		protected void sections() {
			new GSDiv(this, ColorTitleCompositeFlexElement.this.getReverseDirection()) {
				{
					bindStyle("background-color", ReactorStatics.BACKGROUND, model -> getGenericStringProperty(model));
					forEach(ColorTitleCompositeFlexElement.this);
					new HtmlLabel(this) {
						{
							bindText();
						}
					};
				}
			};
		}
	}

	public static class CompositeRadio extends GSComposite {

		public CompositeRadio(Tag parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
		}

		@Override
		protected void sections() {
			new GSDiv(this, CompositeRadio.this.getReverseDirection()) {
				{
					addStyle("flex", "1");
					forEach(CompositeRadio.this);
					new HtmlRadio(this);
					new HtmlLabel(this) {
						{
							bindText();
						}
					};
				}
			};
		}

	}

	public static class ColorCompositeRadio extends GSComposite implements SelectionDefaults {

		private Tag flexSubElement;

		public ColorCompositeRadio(Tag parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
			createSelectionProperty();
			bindBiDirectionalSelection(flexSubElement);
			bindStyle("background-color", SELECTION_STRING);
			addStyle("padding", "4px");
		}

		@Override
		public HtmlDomNode createNode(HtmlDomNode parent, Context modelContext) {
			return new HtmlDomNode(parent, modelContext, this) {

				@Override
				public void handleMessage(JsonObject json) {
					if (UPDATE.equals(json.getString(MSG_TYPE))) {
						((SelectionDefaults) getTag()).getSelectionIndex(getModelContext()).setValue(json.getInteger(SELECTED_INDEX));
					}
				}
			};
		}

		@Override
		protected void sections() {
			flexSubElement = new HtmlLabel(this) {
				{
					addStyle("flex", "1");
					addStyle("justify-content", "center");
					addStyle("align-items", "center");
					addStyle("text-align", "center");
					forEach(ColorCompositeRadio.this);
					bindText();
					bindStyle("background-color", ReactorStatics.BACKGROUND, model -> getGenericStringProperty(model));
					new HtmlRadio(this) {
						{
							addStyle("float", "left");
							addStyle("vertical-align", "middle");
							addStyle("margin", "4px");
						}
					};
				}
			};
		}
	}
}