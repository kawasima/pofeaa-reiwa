/**
 * repositoryとの違いは、データベースアクセスをリポジトリを介して隠すのではなく、
 * ドメインの振る舞いの外におく。
 * なので、コントローラでデータベースアクセスをDataMapperを介して行います。
 * DataMapperの入出力はjOOQのRecord型で行います。
 */
package pofeaa.combination.domain.mapper;