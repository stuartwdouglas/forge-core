package org.jboss.forge.shell.plugins;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;


/**
 * Used to create instances of the {@link Alias} annotation for performing bean lookups with {@link BeanManager}
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class AliasLiteral extends AnnotationLiteral<Alias> implements Alias
{
    private static final long serialVersionUID = -2981610076971124230L;
    private String value;

    public AliasLiteral(String value)
    {
        this.value = value;
    }

    @Override
    public String value()
    {
        return value;
    }
}