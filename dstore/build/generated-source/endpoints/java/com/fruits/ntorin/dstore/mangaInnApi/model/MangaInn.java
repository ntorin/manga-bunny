/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2016-07-08 17:28:43 UTC)
 * on 2016-09-26 at 17:51:40 UTC 
 * Modify at your own risk.
 */

package com.fruits.ntorin.dstore.mangaInnApi.model;

/**
 * Model definition for MangaInn.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the mangaInnApi. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class MangaInn extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String artist;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String author;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String cover;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String genres;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String href;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer rank;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean status;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String title;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getArtist() {
    return artist;
  }

  /**
   * @param artist artist or {@code null} for none
   */
  public MangaInn setArtist(java.lang.String artist) {
    this.artist = artist;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getAuthor() {
    return author;
  }

  /**
   * @param author author or {@code null} for none
   */
  public MangaInn setAuthor(java.lang.String author) {
    this.author = author;
    return this;
  }

  /**
   * @see #decodeCover()
   * @return value or {@code null} for none
   */
  public java.lang.String getCover() {
    return cover;
  }

  /**

   * @see #getCover()
   * @return Base64 decoded value or {@code null} for none
   *
   * @since 1.14
   */
  public byte[] decodeCover() {
    return com.google.api.client.util.Base64.decodeBase64(cover);
  }

  /**
   * @see #encodeCover()
   * @param cover cover or {@code null} for none
   */
  public MangaInn setCover(java.lang.String cover) {
    this.cover = cover;
    return this;
  }

  /**

   * @see #setCover()
   *
   * <p>
   * The value is encoded Base64 or {@code null} for none.
   * </p>
   *
   * @since 1.14
   */
  public MangaInn encodeCover(byte[] cover) {
    this.cover = com.google.api.client.util.Base64.encodeBase64URLSafeString(cover);
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getGenres() {
    return genres;
  }

  /**
   * @param genres genres or {@code null} for none
   */
  public MangaInn setGenres(java.lang.String genres) {
    this.genres = genres;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getHref() {
    return href;
  }

  /**
   * @param href href or {@code null} for none
   */
  public MangaInn setHref(java.lang.String href) {
    this.href = href;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public MangaInn setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getRank() {
    return rank;
  }

  /**
   * @param rank rank or {@code null} for none
   */
  public MangaInn setRank(java.lang.Integer rank) {
    this.rank = rank;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getStatus() {
    return status;
  }

  /**
   * @param status status or {@code null} for none
   */
  public MangaInn setStatus(java.lang.Boolean status) {
    this.status = status;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getTitle() {
    return title;
  }

  /**
   * @param title title or {@code null} for none
   */
  public MangaInn setTitle(java.lang.String title) {
    this.title = title;
    return this;
  }

  @Override
  public MangaInn set(String fieldName, Object value) {
    return (MangaInn) super.set(fieldName, value);
  }

  @Override
  public MangaInn clone() {
    return (MangaInn) super.clone();
  }

}
