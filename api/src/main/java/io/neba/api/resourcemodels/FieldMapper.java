/**
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package io.neba.api.resourcemodels;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * OSGi services implementing this interface may customize the mapping of arbitrary fields during
 * the resource to model mapping of any resource model.
 * <p>
 * The implementing service specifies {@link #getAnnotationType() the annotation it is responsible for}
 * as well as {@link #getFieldType() the type the mapped fields} must be {@link Class#isAssignableFrom(Class) assignable to}.
 * </p>
 * <p>
 * For example, the following service would be responsible for fields annotated with &#64;MyAnnotation with a type <em>assignable to</em> {@link java.util.Collection},
 * e.g. List&lt;Resource&gt; or Set&lt;String&gt;.
 * </p>
 * <pre>
 * &#64;Service
 * &#64;Component(immediate = true)
 * public class MyFieldMapper&lt;Collection, MyAnnotation&gt; {
 *     public Class&lt;? extends Collection&gt; getFieldType() {
 *         return Collection.class;
 *     }
 *
 *     public Class&lt;Annotation&gt; getAnnotationType() {
 *         return MyAnnotation.class;
 *     }
 *
 *     public Collection map(OngoingMapping&lt;Collection, MyAnnotation&gt; ongoingMapping) {
 *         ...
 *     }
 * }
 * </pre>
 * <p>
 * Custom mappers are always invoked <em>after</em> all of NEBA's standard mappings have occurred, but before the corresponding value was set on the model's field.
 * They may thus make use of the already resolved value or choose to provide a different one.
 * </p>
 *
 * @author Olaf Otto
 */
public interface FieldMapper<FieldType, AnnotationType extends Annotation> {
    /**
     * Represents the contextual data of a field mapping during a resource to model mapping.
     *
     * @author Olaf Otto
     */
    public interface OngoingMapping<FieldType, AnnotationType> {
        /**
         * @return The currently resolved value of the field,
         * or <code>null</code> if no value could be resolved for the field.
         */
        FieldType getResolvedValue();

        /**
         * @return The instance of {@link #getAnnotationType() the annotation this mapper is registered for}.
         *         Never <code>null</code>.
         */
        AnnotationType getAnnotation();

        /**
         * @return The mapped model. At this point, the mapping is still incomplete and
         *         no post-processors have been invoked on the model. Never <code>null</code>.
         */
        Object getModel();

        /**
         * @return the mapped field. Never <code>null</code>.
         */
        Field getField();

        /**
         * @return All annotations (including meta-annotations, i.e. annotations of annotations) present on the field.
         *         Never <code>null</code>.
         */
        Map<Class<? extends Annotation>, Annotation> getAnnotationsOfField();

        /**
         * @return The mapped type of the field. <em>This may not be the {@link java.lang.reflect.Field#getType() field type}</em>,
         *         but the component type in case of collections, arrays and {@link io.neba.api.resourcemodels.Optional}
         *         fields as NEBA resolves these types to determine the target type of a mapping.
         *         Never <code>null</code>.
         */
        Class<?> getFieldType();

        /**
         * @return the path of a field, as determined by the field name or {@link io.neba.api.annotations.Path path annotation}. Placeholders
         *         in the path are resolved at this point. Never <code>null</code>.
         */
        String getFieldPath();

        /**
         * @return The resource that is mapped to the model. Never <code>null</code>, but may be a synthetic resource.
         */
        Resource getResource();

        /**
         * @return The {@link org.apache.sling.api.resource.ValueMap} representation of the {@link #getResource() resource}.
         *         This value map does support primitive types, e.g. {@link int.class}. May be <code>null</code> if the resource
         *         has no properties, e.g. if it is synthetic.
         */
        ValueMap getProperties();
    }

    /**
     * @return never <code>null</code>.
     */
    Class<? super FieldType> getFieldType();

    /**
     * @return never <code>null</code>.
     */
    Class<AnnotationType> getAnnotationType();

    /**
     * @param ongoingMapping never <code>null</code>.
     *
     * @return the value to be set on the mapped field during the resource to model mapping. Can be
     *         <code>null</code>.
     */
    FieldType map(OngoingMapping<FieldType, AnnotationType> ongoingMapping);
}
