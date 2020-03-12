/**
 * Modified after
 * https://stackoverflow.com/questions/43093799/hibernate-database-schema-validation-fails-for-h2-database-when-entity-contains
 * Author: https://stackoverflow.com/users/4635513/l00tr
 */
package org.hibernate.dialect;

import java.sql.Types;
import org.hibernate.dialect.H2Dialect;

public class H2DialectCustom extends H2Dialect {
/**
 * This custom H2 dialect is necessary to fix schema validation 
 * which otherwise fails on UUID fields.
 */

    public H2DialectCustom() {
        super();
        registerColumnType(Types.VARBINARY, "uuid");
    }

}
