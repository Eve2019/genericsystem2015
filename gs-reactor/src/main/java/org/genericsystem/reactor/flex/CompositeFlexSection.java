package org.genericsystem.reactor.flex;

import org.genericsystem.reactor.Tag;
import org.genericsystem.reactor.composite.CompositeTag;
import org.genericsystem.reactor.html.HtmlH1;
import org.genericsystem.reactor.html.HtmlLabel;
import org.genericsystem.reactor.html.HtmlRadio;
import org.genericsystem.reactor.model.GenericModel;

/**
 * @author Nicolas Feybesse
 *
 */
public class CompositeFlexSection extends FlexSection implements CompositeTag<GenericModel> {

	public CompositeFlexSection(Tag<?> parent) {
		this(parent, FlexDirection.COLUMN);
	}

	public CompositeFlexSection(Tag<?> parent, FlexDirection flexDirection) {
		super(parent, flexDirection);
		header();
		sections();
		footer();
	}

	protected void header() {

	}

	protected void sections() {
		new FlexSection(this, CompositeFlexSection.this.getReverseDirection()) {
			{
				forEach(CompositeFlexSection.this);
				new HtmlLabel<GenericModel>(this).bindText(GenericModel::getString);
			}
		};
	}

	protected void footer() {
	}

	public static class TitleCompositeFlexElement extends CompositeFlexSection {

		public TitleCompositeFlexElement(Tag<?> parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
		}

		public TitleCompositeFlexElement(Tag<?> parent) {
			this(parent, FlexDirection.COLUMN);
		}

		@Override
		protected void header() {
			new FlexSection(this, FlexDirection.ROW) {
				{
					addStyle("justify-content", "center");
					addStyle("background-color", "#ffa500");
					new HtmlH1<GenericModel>(this) {
						{
							bindText(GenericModel::getString);
						}
					};
				};
			};
		}
	}

	public static class ColorTitleCompositeFlexElement extends TitleCompositeFlexElement {

		public ColorTitleCompositeFlexElement(Tag<?> parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
		}

		public ColorTitleCompositeFlexElement(Tag<?> parent) {
			this(parent, FlexDirection.COLUMN);
		}

		@Override
		protected void sections() {
			new FlexSection(this, ColorTitleCompositeFlexElement.this.getReverseDirection()) {
				{
					bindStyle("background-color", GenericModel::getString);
					forEach(ColorTitleCompositeFlexElement.this);
					new HtmlLabel<GenericModel>(this).bindText(GenericModel::getString);
				}
			};
		}
	}

	public static class CompositeRadio extends CompositeFlexSection implements CompositeTag<GenericModel> {

		public CompositeRadio(Tag<?> parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
		}

		@Override
		protected void sections() {
			new FlexSection(this, CompositeRadio.this.getReverseDirection()) {
				{
					forEach(CompositeRadio.this);
					new HtmlRadio<GenericModel>(this);
					new HtmlLabel<GenericModel>(this) {
						{
							bindText(GenericModel::getString);
						}
					};
				}
			};
		}

	}

	public static class ColorCompositeRadio extends CompositeFlexSection implements CompositeTag<GenericModel> {

		private Tag<GenericModel> flexSubElement;

		public ColorCompositeRadio(Tag<?> parent, FlexDirection flexDirection) {
			super(parent, flexDirection);
			bindBiDirectionalSelection(flexSubElement);
			bindStyle("background-color", GenericModel::getSelectionString);
			addStyle("padding", "4px");
		}

		@Override
		protected HtmlDomNode createNode(String parentId) {
			return new SelectableHtmlDomNode(parentId);
		}

		@Override
		protected void sections() {
			flexSubElement = new FlexSection(this, ColorCompositeRadio.this.getReverseDirection()) {
				{
					forEach(ColorCompositeRadio.this);
					bindStyle("background-color", GenericModel::getString);
					new HtmlRadio<GenericModel>(this);
					new HtmlLabel<GenericModel>(this) {
						{
							bindText(GenericModel::getString);
						}
					};
				}
			};
		}

	}

}
