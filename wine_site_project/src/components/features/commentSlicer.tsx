import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { getReviews, createReview } from "../../api/products";
import { CommentResponseType, CommentType } from "../../types/Product";


type Comment = CommentType & CommentResponseType;

type CommentState = {
  items: CommentResponseType[],
  loaded: boolean,
  hasError: string | null,
};

const commentState: CommentState = {
  items: [],
  loaded: true,
  hasError: null,
};

export const commentsInit = createAsyncThunk<CommentResponseType[], number>(
  'comments/fetch',
  async (productId: number) => {
    const response = await getReviews(productId);

    return response.map((comment: { id: any; userFirstName: any; userLastName: any; message: any; rating: any; reviewDate: string }) => ({
      id: comment.id,
      userFirstName: comment.userFirstName,
      userLastName: comment.userLastName,
      message: comment.message,
      rating: comment.rating,
      reviewDate: comment.reviewDate,
    }));
  }
);

export const addComment = createAsyncThunk<CommentType, CommentType>('comments/add',
    async (comment: CommentType) => {
      const response = await createReview(comment);
      return response as CommentType;
    }
  );

const commentsSlice = createSlice({
  name: 'comments',
  initialState: commentState,
  reducers: {
    commentPush: (state, action: PayloadAction<CommentType>) => {
      state.items.push(action.payload as CommentResponseType);
    }
  },
  extraReducers: (builder) => {
    builder.addCase(commentsInit.pending, (state) => {
      state.loaded = true;
    });
    builder.addCase(commentsInit.fulfilled, (state, action) => {
      state.items = action.payload;
      state.loaded = false;
    });
    builder.addCase(commentsInit.rejected, (state) => {
      state.loaded = false;
      state.hasError = 'Error';
    });
      builder.addCase(addComment.pending, (state) => {
      state.loaded = true;
    });
    builder.addCase(addComment.fulfilled, (state, action) => {
      state.items.push(action.payload as CommentResponseType);
      state.loaded = false;
    });
    builder.addCase(addComment.rejected, (state) => {
      state.loaded = false;
      state.hasError = 'Error';
    });
  }
});

export const {
  commentPush
} = commentsSlice.actions;

export default commentsSlice.reducer;