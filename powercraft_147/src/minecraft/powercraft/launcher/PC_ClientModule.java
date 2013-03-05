package powercraft.launcher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

import powercraft.management.PC_LangEntry;
import powercraft.management.PC_Struct2;
import powercraft.management.registry.PC_LangRegistry;
import powercraft.management.registry.PC_LangRegistry.LangEntry;

/**
 * 
 * A Client PowerCraft need this Annontation to be detected as Client module
 * 
 * @author XOR
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PC_ClientModule {

	/**
	 * 
	 * Function for Language Init
	 * @param list Function input param: {@link List}<{@link PC_LangRegistry.PC_LangEntry}>
	 * @return Function output param: {@link List}<{@link PC_LangRegistry.PC_LangEntry}>
	 * @author XOR
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface PC_InitLanguage{}
	
	/**
	 * 
	 * Function for Textures Loads
	 * @param list Function input param: {@link List}<{@link String}>
	 * @return Function output param: {@link List}<{@link String}>
	 * @author XOR
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface PC_LoadTextureFiles{}
	
	/**
	 * 
	 * Function for adding Splashes
	 * @param list Function input param: {@link List}<{@link String}>
	 * @return Function output param: {@link List}<{@link String}>
	 * @author XOR
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface PC_AddSplashes{}
	
	/**
	 * 
	 * Function for register entity renders
	 * @param list Function input param: {@link List}<{@link PC_Struct2}<{@link Class}< ? extends {@link Entity}>, {@link Render}>>
	 * @return Function output param: {@link List}<{@link PC_Struct2}<{@link Class}< ? extends {@link Entity}>, {@link Render}>>
	 * @author XOR
	 *
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface PC_RegisterEntityRender{}
	
}
