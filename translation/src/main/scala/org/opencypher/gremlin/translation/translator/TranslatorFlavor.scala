/*
 * Copyright (c) 2018 "Neo4j, Inc." [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.gremlin.translation.translator

import org.opencypher.gremlin.translation.ir.rewrite._
import org.opencypher.gremlin.translation.ir.verify._

/**
  * A flavor defines translation rewriting rules and post-conditions.
  */
sealed case class TranslatorFlavor private[translation] (
    rewriters: Seq[GremlinRewriter],
    postConditions: Seq[GremlinPostCondition]) {
  def extend(rewriters: Seq[GremlinRewriter], postConditions: Seq[GremlinPostCondition]): TranslatorFlavor =
    TranslatorFlavor(this.rewriters ++ rewriters, this.postConditions ++ postConditions)

  def extend(rewriters: Seq[GremlinRewriter]): TranslatorFlavor =
    TranslatorFlavor(this.rewriters ++ rewriters, this.postConditions)
}

object TranslatorFlavor {

  /**
    * A translator flavor that is suitable
    * for a fully-compliant Gremlin Server or a compatible graph database
    * with Cypher for Gremlin plugin.
    *
    * This is the default flavor.
    */
  val gremlinServer: TranslatorFlavor = TranslatorFlavor(
    rewriters = Seq(
      InlineFlatMapTraversal,
      RemoveMultipleAliases,
      SimplifyPropertySetters,
      SimplifyRenamedAliases,
      GroupStepFilters,
      SimplifySingleProjections,
      RemoveUselessNullChecks,
      RemoveIdentityReselect,
      RemoveUnusedAliases,
      SimplifyEdgeTraversal,
      SimplifyDelete,
      RemoveUnusedAliases,
      RemoveUselessSteps
    ),
    postConditions = Seq(
      NoEmptyTraversals
    )
  )

  /**
    * A translator flavor that is suitable for Cosmos DB.
    */
  val cosmosDb: TranslatorFlavor = gremlinServer.extend(
    rewriters = Seq(
      CosmosDbFlavor
    ),
    postConditions = Nil
  )

  /**
    * A translator flavor that is suitable for AWS Neptune.
    */
  val neptune: TranslatorFlavor = gremlinServer.extend(
    rewriters = Seq(
      NeptuneFlavor
    ),
    postConditions = Nil
  )

  /**
    * Empty translator flavor without rewriting and post conditions.
    */
  val empty: TranslatorFlavor = TranslatorFlavor(
    rewriters = Nil,
    postConditions = Nil
  )

}
