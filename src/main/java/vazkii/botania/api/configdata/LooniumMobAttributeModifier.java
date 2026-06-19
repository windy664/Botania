package vazkii.botania.api.configdata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class LooniumMobAttributeModifier {
	public static final Codec<LooniumMobAttributeModifier> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
					Identifier.CODEC.fieldOf("id").forGetter(mam -> mam.id),
					BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(mam -> mam.attribute),
					Codec.DOUBLE.fieldOf("amount").forGetter(mam -> mam.amount),
					AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(mam -> mam.operation)
			).apply(instance, LooniumMobAttributeModifier::new)
	);

	private final Identifier id;
	public final Holder<Attribute> attribute;
	private final double amount;
	private final AttributeModifier.Operation operation;

	public LooniumMobAttributeModifier(Identifier id, Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
		this.id = id;
		this.attribute = attribute;
		this.amount = amount;
		this.operation = operation;
	}

	public AttributeModifier createAttributeModifier() {
		return new AttributeModifier(id, amount, operation);
	}

	@Override
	public String toString() {
		return "MobAttributeModifier{" +
				"id='" + id + '\'' +
				", attribute=" + attribute +
				", amount=" + amount +
				", operation=" + operation +
				'}';
	}
}
