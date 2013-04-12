/*
 * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.fge.jsonpatch.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;

/**
 * Difference representation. Captures diff information required to
 * generate JSON patch operations and factorize differences.
 */
final class Diff
{
    DiffOperation operation;
    JsonPointer path;
    JsonPointer arrayPath;
    int firstArrayIndex;
    int secondArrayIndex;
    final JsonNode value;
    JsonPointer fromPath;
    Diff pairedDiff;
    boolean firstOfPair;

    Diff(final DiffOperation operation, final JsonPointer path,
        final JsonNode value)
    {
        this.operation = operation;
        this.path = path;
        this.value = value;
    }

    Diff(final DiffOperation operation, final JsonPointer arrayPath,
        final int firstArrayIndex, final int secondArrayIndex,
        final JsonNode value)
    {
        this.operation = operation;
        this.arrayPath = arrayPath;
        this.firstArrayIndex = firstArrayIndex;
        this.secondArrayIndex = secondArrayIndex;
        this.value = value;
    }

    JsonNode asJsonPatch()
    {
        final JsonPointer ptr = arrayPath != null ? getSecondArrayPath()
            : path;
        final ObjectNode patch = operation.newOp(ptr);
            /*
             * A remove only has a path
             */
        if (operation == DiffOperation.REMOVE)
            return patch;
            /*
             * A move has a "source path" (the "from" member), other defined
             * operations (add and replace) have a value instead.
             */
        if (operation == DiffOperation.MOVE
            || operation == DiffOperation.COPY)
            patch.put("from", fromPath.toString());
        else
            patch.put("value", value);
        return patch;
    }

    JsonPointer getFirstArrayPath()
    {
        // compute path from array path and index
        return arrayPath.append(firstArrayIndex);
    }

    JsonPointer getSecondArrayPath()
    {
        // compute path from array path and index
        if (secondArrayIndex != -1)
            return arrayPath.append(secondArrayIndex);
        return arrayPath.append("-");
    }

    @Override
    public String toString()
    {
        return asJsonPatch().toString();
    }
}
